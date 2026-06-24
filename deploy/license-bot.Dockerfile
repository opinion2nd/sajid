FROM node:20-bookworm-slim
WORKDIR /app
COPY license-bot/package*.json ./
# tsx (the runtime used to start the bot) is a devDependency, so install all deps.
RUN npm ci
COPY license-bot/ ./
CMD ["npm", "start"]
