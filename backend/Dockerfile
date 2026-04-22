FROM node:20-alpine

WORKDIR /app/backend

COPY package.json package-lock.json ./
COPY tsconfig.base.json ./
COPY apps ./apps
COPY packages ./packages

RUN npm ci
