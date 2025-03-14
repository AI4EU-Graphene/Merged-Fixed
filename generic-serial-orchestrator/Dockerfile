# Dockerfile for building generic-serial-orchestrator image from Ubuntu 18.04 image
FROM ubuntu:18.04

# Install necessary system packages
RUN apt-get update \
    && apt-get install -y \
    python3.8 \
    python3.8-dev \
    python3.8-distutils \
    python3-pip \
    python3.8-venv \
    autoconf \
    automake \
    cmake \
    curl \
    g++ \
    git \
    graphviz \
    libatlas3-base \
    libtool \
    make \
    pkg-config \
    sox \
    subversion \
    unzip \
    wget \
    zlib1g-dev \
    vim \
    nano \
    build-essential \
    libopenblas-dev \
    liblapack-dev 

# Set Python 3.8 as the default version
RUN ln -sf /usr/bin/python3.8 /usr/bin/python \
    && ln -sf /usr/bin/pip3 /usr/bin/pip

# Set environment variables to avoid writing bytecode
RUN export PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install Cython separately to prevent numpy build issues
RUN pip install --upgrade pip \
    Cython==0.29.33

# Install numpy separately to avoid build errors
RUN pip install --upgrade pip && pip install numpy==1.19.5 \
    glob2 \
    zipfile36

# Install gRPC dependencies
RUN pip install grpcio \
    grpcio-tools \
    googleapis-common-protos

# Copy required files into the container (fixed the typo)
COPY ./orchestrator_container /orchestrator_container
RUN ls -laR /orchestrator_container/*

# Set the working directory inside the container
WORKDIR /orchestrator_container

# Start the orchestrator server
CMD [ "python3", "./orchestrator_server.py" ]