# Usar uma imagem oficial do PHP com servidor embutido
FROM php:8.1-cli

# Copiar arquivos do projeto para dentro do container
COPY . /var/www/html

# Mudar o diretório de trabalho
WORKDIR /var/www/html

# Expor a porta 8080
EXPOSE 8080

# Comando para rodar o servidor embutido do PHP
CMD ["php", "-S", "0.0.0.0:8080"]
