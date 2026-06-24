FROM node:20-bookworm-slim
WORKDIR /app
COPY license-bot/package*.json ./
RUN npm ci --omit=dev
COPY license-bot/ ./
CMD ["npx", "tsx", "src/index.ts"]
