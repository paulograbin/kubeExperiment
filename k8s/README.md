# Kubernetes Deployment for kube-experiment

This directory contains Kubernetes manifests and a Helm chart for deploying the kube-experiment Spring Boot application on Minikube with Istio.

## Prerequisites

- Docker installed and configured
- Minikube installed (using Docker driver)
- kubectl installed
- Helm installed
- Docker Hub account (username: paulograbin)

## Quick Start (Helm)

```bash
# Start minikube
minikube start --driver=docker

# Install Istio (if not already installed)
istioctl install --set profile=demo -y

# Deploy with Helm
helm upgrade --install kube-experiment ./chart

# Wait for pods to be ready
kubectl rollout status deployment/kube-experiment-deployment -n multivac

# Access the app
curl -H "Host: kube-experiment.multivac.local" http://$(minikube ip):30719/
```

## Build and Push Docker Image

```bash
# Build the Docker image
docker build -t paulograbin/kube-experiment:latest .

# Login to Docker Hub
docker login

# Push the image to Docker Hub
docker push paulograbin/kube-experiment:latest
```

## Deploy with Helm Chart

The Helm chart is in the `chart/` directory and manages all resources:

```bash
# Install/upgrade
helm upgrade --install kube-experiment ./chart

# Check status
helm status kube-experiment

# Uninstall
helm uninstall kube-experiment
```

### Helm Values

Key values in `chart/values.yaml`:

| Value | Default | Description |
|-------|---------|-------------|
| `applications.namespace` | `multivac` | Target namespace |
| `backend.deployment.replicas` | `4` | Number of pod replicas |
| `backend.service.ports.port` | `80` | Service port |
| `backend.service.ports.targetPort` | `8080` | Container port |

## Deploy with Plain Manifests

Apply the manifests in order:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f gateway.yaml
kubectl apply -f virtualservice.yaml
```

Or all at once:

```bash
kubectl apply -f .
```

## TLS Certificate (optional, for HTTPS)

```bash
# Generate a self-signed certificate
openssl req -x509 -nodes -days 365 \
  -newkey rsa:2048 \
  -keyout kube-experiment.key \
  -out kube-experiment.crt \
  -subj "/CN=kube-experiment.multivac.local"

# Create the TLS secret in istio-system namespace
kubectl create -n istio-system secret tls kube-experiment-tls \
  --cert=kube-experiment.crt \
  --key=kube-experiment.key
```

## Accessing the Application

The app is exposed via Istio Gateway on the hostname `kube-experiment.multivac.local`.

### Setup /etc/hosts

Add the minikube IP to your hosts file:

```bash
echo "$(minikube ip)	kube-experiment.multivac.local" | sudo tee -a /etc/hosts
```

### Access via browser

```
http://kube-experiment.multivac.local:30719/
```

> **Note:** You must use the hostname, not the raw IP. Istio routes based on the `Host` header — hitting the IP directly returns a 404.

### Access via curl

```bash
# Using hostname (requires /etc/hosts entry)
curl http://kube-experiment.multivac.local:30719/

# Using IP with explicit Host header
curl -H "Host: kube-experiment.multivac.local" http://$(minikube ip):30719/

# Health check
curl http://kube-experiment.multivac.local:30719/actuator/health
```

### Port reference

| Port | Where |
|------|-------|
| `8080` | Container (Spring Boot) |
| `80` | ClusterIP Service |
| `30719` | Istio ingress gateway NodePort (HTTP) |
| `32527` | Istio ingress gateway NodePort (HTTPS) |

## Verify Deployment

```bash
# Check all resources
kubectl get all -n multivac

# Check pods (should show 2/2 — app + istio sidecar)
kubectl get pods -n multivac

# View pod logs
kubectl logs -n multivac -l app=kube-experiment -c kube-experiment

# Follow logs
kubectl logs -n multivac -l app=kube-experiment -c kube-experiment -f

# Check Istio proxy status
istioctl proxy-status
```

## Health Check Endpoints

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- All actuator endpoints: `/actuator`

## Scaling

```bash
# Scale via kubectl
kubectl scale deployment kube-experiment-deployment -n multivac --replicas=3

# Or update chart/values.yaml and re-deploy
helm upgrade kube-experiment ./chart
```

## Updating the Application

```bash
# Build and push new image
docker build -t paulograbin/kube-experiment:latest .
docker push paulograbin/kube-experiment:latest

# Restart deployment to pull new image
kubectl rollout restart deployment kube-experiment-deployment -n multivac

# Watch rollout
kubectl rollout status deployment kube-experiment-deployment -n multivac
```

## Troubleshooting

```bash
# Describe pod for events (startup probe failures, image pull errors, etc.)
kubectl describe pod -n multivac -l app=kube-experiment

# Get pod logs (specify container since istio sidecar is present)
kubectl logs -n multivac -l app=kube-experiment -c kube-experiment --tail=100

# Check service endpoints
kubectl get endpoints kube-experiment-service -n multivac

# Test connectivity from inside the cluster
kubectl run curl-test --rm -i --restart=Never --image=curlimages/curl -n multivac \
  -- curl -s http://kube-experiment-service:80/actuator/health

# Check Istio routing
istioctl proxy-config routes -n istio-system deploy/istio-ingressgateway
```

### Common issues

| Symptom | Cause | Fix |
|---------|-------|-----|
| Pods in `CrashLoopBackOff` | App takes longer to start than liveness probe allows | Increase `startupProbe.failureThreshold` in values.yaml |
| Pods `0/2` or `1/2` Ready | Istio sidecar not injecting or app not ready | Check namespace has `istio-injection: enabled` label |
| 404 from gateway | Wrong `Host` header or VirtualService misconfigured | Use the hostname, not raw IP |
| 503 from gateway | Service name mismatch or pods not ready | Verify VirtualService destination matches service name |

## Clean Up

```bash
# Remove Helm release
helm uninstall kube-experiment

# Or delete the namespace (removes everything)
kubectl delete namespace multivac
```

## Configuration Reference

### Probes

The deployment uses three probes:

| Probe | Purpose | Timing |
|-------|---------|--------|
| **startupProbe** | Gives the JVM time to boot | 10s delay, then check every 2s, up to 15 failures (40s max) |
| **livenessProbe** | Restarts unhealthy pods | Every 5s, 3 failures to kill |
| **readinessProbe** | Removes from service until ready | Every 5s, 3 failures to remove |

### Resource Limits

- **Memory**: 256Mi request / 512Mi limit
- **CPU**: 250m request / 500m limit

### Istio

- Sidecar injection enabled via namespace label
- Gateway accepts HTTP (80) and HTTPS (443)
- VirtualService routes all paths (`/`) to the service
