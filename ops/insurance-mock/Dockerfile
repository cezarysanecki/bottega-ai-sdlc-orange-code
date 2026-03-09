FROM node:18-alpine
WORKDIR /srv
COPY server.js server.js
RUN npm init -y >/dev/null 2>&1 \
  && npm install express --quiet
EXPOSE 8081
CMD ["node", "server.js"]
