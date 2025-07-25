#!/bin/sh

# Replace environment variables in built files
if [ -f /usr/share/nginx/html/index.html ]; then
    # Replace API base URL if provided
    if [ ! -z "$VITE_API_BASE_URL" ]; then
        sed -i "s|__VITE_API_BASE_URL__|$VITE_API_BASE_URL|g" /usr/share/nginx/html/assets/*.js 2>/dev/null || true
    fi
    
    # Replace WebSocket URL if provided
    if [ ! -z "$VITE_WS_HOST" ]; then
        sed -i "s|__VITE_WS_HOST__|$VITE_WS_HOST|g" /usr/share/nginx/html/assets/*.js 2>/dev/null || true
    fi
fi

# Start nginx
exec "$@"