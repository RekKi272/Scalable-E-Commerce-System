FROM nginx:1.25

# copy base config
COPY nginx.conf /etc/nginx/nginx.conf

# copy prod config
COPY nginx.prod.conf /etc/nginx/conf.d/default.conf

# copy SSL cert for prod
COPY ssl/prod /etc/nginx/ssl
