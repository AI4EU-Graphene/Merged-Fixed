### STAGE 0: Build - "build-stage", based on Node.js, to build and compile the frontend ###

# We label our stage as build-stage
FROM node:10-alpine as build-stage

WORKDIR /app

COPY package*.json /app/

COPY ./ /app/

## Storing node modules on a separate layer will prevent unnecessary npm installs at each build
RUN npm install

## generating src/environments/app.version.ts file
RUN npm run prebuild

# default configuration to build for production
ARG configuration=production

## Build the angular app in mode based on configuration arg and store the artifacts in dist/license-rtu-editor folder
RUN npm run build --  --base-href /license-rtu-editor/ --output-path=./dist/license-rtu-editor --configuration $configuration

### STAGE 1: Build - "test-stage", based on Node.js & puppeteer, to test the frontend ###

# We label our stage as test-stage
FROM paragraval/node-min-puppeteer:1.0.0 as test-stage

WORKDIR /app

## From 'build-stage' stage, copy over the artifacts in app folder
COPY --from=build-stage /app/ /app/

RUN npm run ci-test

### STAGE 2: Setup ###

# based on Nginx, to have only the compiled app, ready for production with Nginx
FROM nginxinc/nginx-unprivileged:1.17-alpine

## From 'build-stage' stage, copy over the artifacts in dist folder to default nginx public folder
COPY --from=build-stage /app/dist/ /usr/share/nginx/html

