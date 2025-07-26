FROM nginx:alpine

# Create the HTML content first
COPY index.html /usr/share/nginx/html/index.html

# Configure nginx to listen on port 3000
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
