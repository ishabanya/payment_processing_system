import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { ThemeConfig } from '@/types'

interface ThemeState {
  // State
  theme: 'light' | 'dark' | 'system'
  primaryColor: string
  accentColor: string
  borderRadius: 'none' | 'small' | 'medium' | 'large'
  compactMode: boolean
  
  // Actions
  setTheme: (theme: 'light' | 'dark' | 'system') => void
  setPrimaryColor: (color: string) => void
  setAccentColor: (color: string) => void
  setBorderRadius: (radius: 'none' | 'small' | 'medium' | 'large') => void
  setCompactMode: (compact: boolean) => void
  resetTheme: () => void
  applyTheme: (config: Partial<ThemeConfig>) => void
}

const defaultTheme: ThemeConfig = {
  mode: 'system',
  primaryColor: '#0ea5e9',
  accentColor: '#8b5cf6',
  borderRadius: 'medium',
  compactMode: false,
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      // Initial state
      theme: defaultTheme.mode,
      primaryColor: defaultTheme.primaryColor,
      accentColor: defaultTheme.accentColor,
      borderRadius: defaultTheme.borderRadius,
      compactMode: defaultTheme.compactMode,

      // Set theme mode
      setTheme: (theme: 'light' | 'dark' | 'system') => {
        set({ theme })
        applyThemeToDocument(theme)
      },

      // Set primary color
      setPrimaryColor: (color: string) => {
        set({ primaryColor: color })
        applyColorToDocument('primary', color)
      },

      // Set accent color
      setAccentColor: (color: string) => {
        set({ accentColor: color })
        applyColorToDocument('accent', color)
      },

      // Set border radius
      setBorderRadius: (radius: 'none' | 'small' | 'medium' | 'large') => {
        set({ borderRadius: radius })
        applyBorderRadiusToDocument(radius)
      },

      // Set compact mode
      setCompactMode: (compact: boolean) => {
        set({ compactMode: compact })
        applyCompactModeToDocument(compact)
      },

      // Reset to default theme
      resetTheme: () => {
        set({
          theme: defaultTheme.mode,
          primaryColor: defaultTheme.primaryColor,
          accentColor: defaultTheme.accentColor,
          borderRadius: defaultTheme.borderRadius,
          compactMode: defaultTheme.compactMode,
        })
        
        // Apply all default settings
        applyThemeToDocument(defaultTheme.mode)
        applyColorToDocument('primary', defaultTheme.primaryColor)
        applyColorToDocument('accent', defaultTheme.accentColor)
        applyBorderRadiusToDocument(defaultTheme.borderRadius)
        applyCompactModeToDocument(defaultTheme.compactMode)
      },

      // Apply complete theme configuration
      applyTheme: (config: Partial<ThemeConfig>) => {
        const currentState = get()
        const newState = {
          theme: config.mode || currentState.theme,
          primaryColor: config.primaryColor || currentState.primaryColor,
          accentColor: config.accentColor || currentState.accentColor,
          borderRadius: config.borderRadius || currentState.borderRadius,
          compactMode: config.compactMode ?? currentState.compactMode,
        }
        
        set(newState)
        
        // Apply all settings to document
        applyThemeToDocument(newState.theme)
        applyColorToDocument('primary', newState.primaryColor)
        applyColorToDocument('accent', newState.accentColor)
        applyBorderRadiusToDocument(newState.borderRadius)
        applyCompactModeToDocument(newState.compactMode)
      },
    }),
    {
      name: 'theme-storage',
      onRehydrateStorage: () => (state) => {
        if (state) {
          // Apply stored theme settings on app load
          applyThemeToDocument(state.theme)
          applyColorToDocument('primary', state.primaryColor)
          applyColorToDocument('accent', state.accentColor)
          applyBorderRadiusToDocument(state.borderRadius)
          applyCompactModeToDocument(state.compactMode)
        }
      },
    }
  )
)

// Helper functions to apply theme changes to the document
function applyThemeToDocument(theme: 'light' | 'dark' | 'system') {
  const root = document.documentElement
  
  if (theme === 'system') {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    root.classList.toggle('dark', mediaQuery.matches)
  } else {
    root.classList.toggle('dark', theme === 'dark')
  }
}

function applyColorToDocument(type: 'primary' | 'accent', color: string) {
  const root = document.documentElement
  const rgb = hexToRgb(color)
  
  if (rgb) {
    const { r, g, b } = rgb
    
    if (type === 'primary') {
      root.style.setProperty('--color-primary-50', generateColorScale(r, g, b, 0.05))
      root.style.setProperty('--color-primary-100', generateColorScale(r, g, b, 0.1))
      root.style.setProperty('--color-primary-200', generateColorScale(r, g, b, 0.2))
      root.style.setProperty('--color-primary-300', generateColorScale(r, g, b, 0.3))
      root.style.setProperty('--color-primary-400', generateColorScale(r, g, b, 0.4))
      root.style.setProperty('--color-primary-500', `${r}, ${g}, ${b}`)
      root.style.setProperty('--color-primary-600', generateColorScale(r, g, b, 0.8))
      root.style.setProperty('--color-primary-700', generateColorScale(r, g, b, 0.9))
      root.style.setProperty('--color-primary-800', generateColorScale(r, g, b, 0.95))
      root.style.setProperty('--color-primary-900', generateColorScale(r, g, b, 0.98))
    } else {
      root.style.setProperty('--color-accent-500', `${r}, ${g}, ${b}`)
    }
  }
}

function applyBorderRadiusToDocument(radius: 'none' | 'small' | 'medium' | 'large') {
  const root = document.documentElement
  
  const radiusValues = {
    none: '0',
    small: '0.25rem',
    medium: '0.5rem',
    large: '1rem',
  }
  
  root.style.setProperty('--border-radius', radiusValues[radius])
}

function applyCompactModeToDocument(compact: boolean) {
  const root = document.documentElement
  root.classList.toggle('compact-mode', compact)
  
  if (compact) {
    root.style.setProperty('--spacing-scale', '0.8')
    root.style.setProperty('--font-size-scale', '0.9')
  } else {
    root.style.setProperty('--spacing-scale', '1')
    root.style.setProperty('--font-size-scale', '1')
  }
}

// Utility functions
function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : null
}

function generateColorScale(r: number, g: number, b: number, opacity: number): string {
  const factor = 1 - opacity
  const newR = Math.round(r + (255 - r) * factor)
  const newG = Math.round(g + (255 - g) * factor)
  const newB = Math.round(b + (255 - b) * factor)
  return `${newR}, ${newG}, ${newB}`
}

// Theme presets
export const themePresets = {
  default: {
    mode: 'system' as const,
    primaryColor: '#0ea5e9',
    accentColor: '#8b5cf6',
    borderRadius: 'medium' as const,
    compactMode: false,
  },
  modern: {
    mode: 'dark' as const,
    primaryColor: '#6366f1',
    accentColor: '#f59e0b',
    borderRadius: 'large' as const,
    compactMode: false,
  },
  minimal: {
    mode: 'light' as const,
    primaryColor: '#64748b',
    accentColor: '#22c55e',
    borderRadius: 'small' as const,
    compactMode: true,
  },
  vibrant: {
    mode: 'light' as const,
    primaryColor: '#ec4899',
    accentColor: '#06b6d4',
    borderRadius: 'medium' as const,
    compactMode: false,
  },
}