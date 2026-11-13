# ==========================================
# Stage 1: Build Frontend Assets
# ==========================================
FROM node:22-alpine AS build

WORKDIR /app

# Copy dependency manifests
COPY package*.json ./

RUN npm install

# Copy application source code
COPY . .

# Build production distribution bundle
RUN npm run build

# ==========================================
# Stage 2: Production Nginx Web Server
# ==========================================
FROM nginx:alpine

# Copy custom Nginx reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
