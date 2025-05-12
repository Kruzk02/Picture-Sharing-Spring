FROM node:22

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install -g @angular/cli

COPY . .

EXPOSE 4200

CMD ["sh", "-c", "npm install && ng serve --host 0.0.0.0 --poll=1000"]
