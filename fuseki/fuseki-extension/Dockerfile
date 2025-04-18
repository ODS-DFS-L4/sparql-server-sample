# Development stage: Set up a development environment
FROM maven:3.8.5-openjdk-11

# Add user `developer`
RUN apt update && apt install -y sudo
RUN useradd -m -u 1000 developer \
    && echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers \
    && adduser developer sudo

RUN id developer && echo "Developer user created successfully"
WORKDIR /app

RUN chown -R developer:developer /app
USER developer

# Set the working directory
WORKDIR /app