# kube-experiment

A Spring Boot application for experimenting with Kubernetes concepts — deployments, probes, Istio service mesh, graceful shutdown, and rolling updates.

The app exposes a single endpoint that returns detailed HTTP request information (headers, pod name, routing metadata), making it easy to observe load balancing, Istio sidecar injection, and traffic routing in action.

## Tech Stack

- **Java 25** / Spring Boot 4.0
- **Spring Modulith** for modular architecture
- **Spring Actuator** for health probes and observability
- **Docker** (multi-stage build with Eclipse Temurin)
- **Kubernetes** (Minikube) with Istio service mesh
- **Helm** for deployment

## Project Structure

```
├── src/                    # Spring Boot application
├── k8s/                    # Kubernetes manifests & Helm chart
│   ├── chart/              # Helm chart (deployment, service, gateway, virtualservice)
│   ├── certs/              # TLS certificates for Istio HTTPS
│   ├── rendered.yaml       # Pre-rendered manifests (Helm-free deploy)
│   └── README.md           # Detailed k8s deployment guide
├── Dockerfile              # Multi-stage build
├── build.gradle            # Gradle build config
└── README.md               # This file
```

## Quick Start

### Prerequisites

- Java 25+
- Docker
- Minikube + kubectl + Helm + istioctl

### Run locally

```bash
./gradlew bootRun
```

The app starts on http://localhost:8080. Hit `/` to see request details, or `/actuator/health` for health status.

### Deploy to Kubernetes

```bash
# Start minikube and install Istio
minikube start --driver=docker
istioctl install --set profile=demo -y

# Build and push the image
docker build -t paulograbin/kube-experiment:latest .
docker push paulograbin/kube-experiment:latest

# Deploy with Helm
cd k8s
helm upgrade --install kube-experiment ./chart

# Wait for rollout
kubectl rollout status deployment/kube-experiment-deployment -n multivac
```

See [k8s/README.md](k8s/README.md) for the full deployment guide (accessing the app, TLS, scaling, troubleshooting).

## Endpoints

| Path | Description |
|------|-------------|
| `GET /` | Returns request details (pod name, headers, client info, routing) |
| `GET /actuator/health` | Health status |
| `GET /actuator/health/liveness` | Liveness probe (used by Kubernetes) |
| `GET /actuator/health/readiness` | Readiness probe (used by Kubernetes) |
| `GET /actuator` | All actuator endpoints |

## Kubernetes Features Explored

- **Startup / Liveness / Readiness probes** — proper configuration for JVM apps with slow startup
- **Graceful shutdown** — `preStop` hook + Spring's graceful shutdown to drain connections
- **Istio service mesh** — sidecar injection, Gateway, VirtualService, mTLS
- **Rolling updates** — zero-downtime deployments with proper probe timing
- **Helm chart** — templated, repeatable deployments with configurable values

## Building

```bash
# Run tests
./gradlew test

# Build fat JAR
./gradlew bootJar

# Build Docker image
docker build -t paulograbin/kube-experiment:latest .
```
