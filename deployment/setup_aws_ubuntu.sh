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

# 0. Configurar Swap (2GB) para prevenir Out-Of-Memory en instancias t2.micro/t3.micro
if [ ! -f /swapfile ]; then
    echo "🧠 [0/7] Creando 2GB de memoria Swap para compilación..."
    sudo fallocate -l 2G /swapfile 2>/dev/null || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "✔ Memoria Swap configurada."
fi

# 1. Actualizar repositorios del sistema
echo "📦 [1/7] Actualizando paquetes de Ubuntu..."
sudo apt-get update -y
sudo apt-get install -y curl git ufw build-essential nginx mysql-server

# 2. Instalar Node.js 20 LTS
echo "🟢 [2/7] Instalando Node.js 20 LTS..."
if ! command -v node &> /dev/null; then
    curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

# Configurar npm para resolver dependencias antiguas de Cloudinary sin bloquearse
sudo npm config set legacy-peer-deps true --global 2>/dev/null || true
npm config set legacy-peer-deps true 2>/dev/null || true

# Instalar PM2 para gestión de procesos en segundo plano
sudo npm install -g pm2

# 3. Configurar Base de Datos MySQL de forma resiliente
echo "🗄️ [3/7] Configurando base de datos MySQL..."
MYSQL_PASS="08042003"
export MYSQL_PWD="${MYSQL_PASS}"

if mysql -u root -e "SELECT 1;" &>/dev/null; then
    echo "✔ MySQL ya responde con la contraseña configurada."
elif sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_PASS}'; FLUSH PRIVILEGES;" &>/dev/null; then
    echo "✔ Contraseña de root asignada mediante sudo mysql."
elif [ -f /etc/mysql/debian.cnf ] && sudo mysql --defaults-file=/etc/mysql/debian.cnf -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_PASS}'; FLUSH PRIVILEGES;" &>/dev/null; then
    echo "✔ Contraseña de root asignada mediante debian.cnf."
else
    echo "⚠️ Reiniciando MySQL temporalmente para ajustar credenciales de root..."
    sudo systemctl stop mysql
    sudo mkdir -p /var/run/mysqld
    sudo chown mysql:mysql /var/run/mysqld
    sudo mysqld_safe --skip-grant-tables --skip-networking &
    sleep 4
    mysql -u root -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_PASS}'; FLUSH PRIVILEGES;" 2>/dev/null || true
    sudo pkill -f mysqld || true
    sleep 2
    sudo systemctl start mysql
fi

mysql -u root -e "CREATE DATABASE IF NOT EXISTS aguaya CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "✔ MySQL configurado y base de datos 'aguaya' lista."

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
mysql -u root aguaya < "$REPO_DIR/aguaya-backend/schema.sql"
mysql -u root aguaya < "$REPO_DIR/aguaya-backend/migracion_v2.sql"

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

npm install --omit=dev --legacy-peer-deps
pm2 stop aguaya-backend 2>/dev/null || true
pm2 start server.js --name "aguaya-backend"
pm2 save

# 7. Compilar Frontend Vue 3
echo "🎨 [6/7] Compilando Frontend Vue 3..."
cd /var/www/aguaya/frontend
npm install --legacy-peer-deps
npm run build

# 8. Configurar Nginx
echo "🌐 [7/7] Configurando Nginx Reverse Proxy..."
sudo cp "$REPO_DIR/deployment/nginx/aguaya.conf" /etc/nginx/sites-available/aguaya

# Habilitar sitio y deshabilitar default
sudo ln -sf /etc/nginx/sites-available/aguaya /etc/nginx/sites-enabled/aguaya
sudo rm -f /etc/nginx/sites-enabled/default

# Ajustar permisos para que Nginx pueda servir los archivos
sudo chmod -R 755 /var/www/aguaya/frontend/dist 2>/dev/null || true

# Verificar y reiniciar Nginx
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl enable nginx

echo "=========================================================="
echo "🎉 ¡Despliegue completado con éxito en AWS!"
echo "   - Web Frontend:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<IP-PUBLICA-AWS>')/"
echo "   - API Endpoint:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<IP-PUBLICA-AWS>')/api/"
echo "=========================================================="
