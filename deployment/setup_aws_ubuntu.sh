#!/bin/bash
# ==============================================================================
#  AguaYa — Script de Despliegue Automatizado para AWS EC2 (Ubuntu 22.04 / 24.04)
#  Ejecutar en la instancia EC2 con:
#    bash setup_aws_ubuntu.sh
# ==============================================================================

set -e

echo "=========================================================="
echo "💧 AguaYa — Despliegue en AWS EC2 (Nginx + Node.js + MySQL)"
echo "=========================================================="

# 1. Actualizar repositorios del sistema
echo "📦 [1/7] Actualizando paquetes de Ubuntu..."
sudo apt-get update -y && sudo apt-get upgrade -y
sudo apt-get install -y curl git ufw build-essential nginx mysql-server

# 2. Instalar Node.js 20 LTS
echo "🟢 [2/7] Instalando Node.js 20 LTS..."
if ! command -v node &> /dev/null; then
    curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

# Instalar PM2 para gestión de procesos en segundo plano
sudo npm install -g pm2

# 3. Configurar Base de Datos MySQL
echo "🗄️ [3/7] Configurando base de datos MySQL..."
MYSQL_PASS="08042003"

sudo mysql -e "CREATE DATABASE IF NOT EXISTS aguaya CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASS}';"
sudo mysql -e "FLUSH PRIVILEGES;"

echo "✔ MySQL configurado con base de datos 'aguaya'."

# 4. Preparar directorios en /var/www/aguaya
echo "📁 [4/7] Preparando directorios en /var/www/aguaya..."
sudo mkdir -p /var/www/aguaya/backend
sudo mkdir -p /var/www/aguaya/frontend
sudo chown -R $USER:$USER /var/www/aguaya

# 5. Obtener repositorio o sincronizar código
REPO_DIR="$HOME/AguaYa-Android"
if [ ! -d "$REPO_DIR" ]; then
    echo "Clonando repositorio AguaYa desde GitHub..."
    git clone https://github.com/Frixmas-dot/AguaYa-Android.git "$REPO_DIR"
else
    echo "Actualizando repositorio existente..."
    cd "$REPO_DIR" && git pull origin main
fi

# Migrar base de datos con los 90+ barrios y soft delete
echo "Poblando base de datos con esquema y barrios oficiales..."
mysql -u root -p${MYSQL_PASS} aguaya < "$REPO_DIR/aguaya-backend/schema.sql"
mysql -u root -p${MYSQL_PASS} aguaya < "$REPO_DIR/aguaya-backend/migracion_v2.sql"

# Copiar archivos al directorio web
cp -r "$REPO_DIR/aguaya-backend/"* /var/www/aguaya/backend/
cp -r "$REPO_DIR/aguaya-frontend/"* /var/www/aguaya/frontend/

# 6. Configurar y arrancar Backend con PM2
echo "🚀 [5/7] Configurando Backend Node.js..."
cd /var/www/aguaya/backend

# Crear archivo .env si no existe
if [ ! -f .env ]; then
cat <<EOF > .env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=${MYSQL_PASS}
DB_NAME=aguaya
PORT=3001

CLOUDINARY_CLOUD_NAME=dvppwbpzz
CLOUDINARY_API_KEY=585941825417648
CLOUDINARY_API_SECRET=-2DT9GgXWB16awNnq8i6jqZQVvo
EOF
fi

npm install --production
pm2 stop aguaya-backend || true
pm2 start server.js --name "aguaya-backend"
pm2 save

# 7. Compilar Frontend Vue 3
echo "🎨 [6/7] Compilando Frontend Vue 3..."
cd /var/www/aguaya/frontend
npm install
npm run build

# 8. Configurar Nginx
echo "🌐 [7/7] Configurando Nginx Reverse Proxy..."
sudo cp "$REPO_DIR/deployment/nginx/aguaya.conf" /etc/nginx/sites-available/aguaya

# Habilitar sitio y deshabilitar default
sudo ln -sf /etc/nginx/sites-available/aguaya /etc/nginx/sites-enabled/aguaya
sudo rm -f /etc/nginx/sites-enabled/default

# Verificar y reiniciar Nginx
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl enable nginx

echo "=========================================================="
echo "🎉 ¡Despliegue completado con éxito en AWS!"
echo "   - Web Frontend:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<IP-PUBLICA-AWS>')/"
echo "   - API Endpoint:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<IP-PUBLICA-AWS>')/api/"
echo "=========================================================="
