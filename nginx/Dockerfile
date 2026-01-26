FROM nginx:1.25

# Xóa config mặc định của nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx config production
COPY nginx.prod.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
