-- Usuarios generales
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    correo VARCHAR(100) UNIQUE,
    contraseña VARCHAR(255),
    rol ENUM('paciente', 'psicologo', 'admin'),
    sexo VARCHAR(20),
    fecha_nacimiento DATE
);

-- Datos específicos del psicólogo
CREATE TABLE psicologos (
    id INT PRIMARY KEY,
    años_experiencia INT,
    resumen TEXT,
    foto_url TEXT,
    FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- Datos específicos del paciente
CREATE TABLE pacientes (
    id INT PRIMARY KEY,
    id_psicologo INT,
    FOREIGN KEY (id) REFERENCES usuarios(id),
    FOREIGN KEY (id_psicologo) REFERENCES psicologos(id)
);

-- Citas entre pacientes y psicólogos
CREATE TABLE citas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT,
    id_psicologo INT,
    fecha DATETIME,
    estado ENUM('pendiente', 'realizada', 'cancelada'),
    es_primera BOOLEAN,
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id),
    FOREIGN KEY (id_psicologo) REFERENCES psicologos(id)
);

-- Crear tabla de auditoría para pacientes
CREATE TABLE auditoria_pacientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_psicologo_anterior INT,
    id_psicologo_nuevo INT,
    fecha_modificacion DATETIME NOT NULL
);

-- Crear tabla de auditoría para citas
CREATE TABLE auditoria_citas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL,
    fecha_anterior DATETIME,
    fecha_nueva DATETIME,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50),
    es_primera_anterior TINYINT(1),
    es_primera_nuevo TINYINT(1),
    fecha_modificacion DATETIME NOT NULL
);

CREATE TABLE especialidades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE psicologo_especialidad (
    id_psicologo INT,
    id_especialidad INT,
    PRIMARY KEY (id_psicologo, id_especialidad),
    FOREIGN KEY (id_psicologo) REFERENCES psicologos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_especialidad) REFERENCES especialidades(id) ON DELETE CASCADE
);