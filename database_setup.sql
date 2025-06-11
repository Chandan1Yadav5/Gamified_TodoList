CREATE DATABASE IF NOT EXISTS gamified_todo;
USE gamified_todo;

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    priority INT NOT NULL,
    is_done BOOLEAN DEFAULT FALSE,
    xp_reward INT NOT NULL
);
