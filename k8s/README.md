# Kubernetes Deployment for kube-experiment

This directory contains Kubernetes manifests for deploying the kube-experiment Spring Boot application to SAP Kyma.

## Prerequisites

- Docker installed and configured
- kubectl installed and configured to access your Kyma cluster
- Docker Hub account (username: paulograbin)

## Step 1: Build and Push Docker Image

```bash
# Build the Docker image
docker build -t paulograbin/kube-experiment:latest .

# Login to Docker Hub
docker login

# Push the image to Docker Hub
docker push paulograbin/kube-experiment:latest
```

## Step 2: Create TLS Certificate (optional, for HTTPS)

```bash
# Generate a self-signed certificate for local/dev
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

## Step 3: Deploy to Kubernetes

Apply the manifests in the following order:

```bash
# Create the namespace
kubectl apply -f k8s/namespace.yaml

# Deploy the application
kubectl apply -f k8s/deployment.yaml

# Create the service
kubectl apply -f k8s/service.yaml

# Create the Istio gateway
kubectl apply -f k8s/gateway.yaml

# Create the Istio virtual service
kubectl apply -f k8s/virtualservice.yaml
```

Or apply all at once:

```bash
kubectl apply -f k8s/
```

## Step 4: Verify Deployment

```bash
# Check all resources in the multivac namespace
kubectl get all -n multivac

# Check deployment status
kubectl get deployment kube-experiment -n multivac

# Check pod status
kubectl get pods -n multivac

# Check service
kubectl get svc kube-experiment -n multivac

# Check Istio gateway
kubectl get gateway kube-experiment-gateway -n multivac

# Check Istio virtual service
kubectl get virtualservice kube-experiment -n multivac

# View pod logs
kubectl logs -n multivac -l app=kube-experiment

# Follow logs
kubectl logs -n multivac -l app=kube-experiment -f
```

## Step 5: Access the Application

The application is exposed via Istio Gateway + VirtualService at:
- Host: `kube-experiment.multivac.local`

Update your `/etc/hosts` file or DNS to point this hostname to your Istio ingress gateway IP.

Get the ingress gateway IP:
```bash
kubectl get svc istio-ingressgateway -n istio-system
```

Then access the application:
```bash
# HTTP
curl http://kube-experiment.multivac.local

# HTTPS (use -k for self-signed certs)
curl -k https://kube-experiment.multivac.local
```

## Health Check Endpoints

The application exposes Spring Boot Actuator endpoints:

- Liveness: `http://kube-experiment.multivac.local/actuator/health/liveness`
- Readiness: `http://kube-experiment.multivac.local/actuator/health/readiness`
- All actuator endpoints: `http://kube-experiment.multivac.local/actuator`

## Scaling

To scale the deployment:

```bash
# Scale to 3 replicas
kubectl scale deployment kube-experiment -n multivac --replicas=3

# Or edit the deployment.yaml and reapply
```

## Updating the Application

After making code changes:

```bash
# Build and push new image
docker build -t paulograbin/kube-experiment:latest .
docker push paulograbin/kube-experiment:latest

# Restart the deployment to pull new image
kubectl rollout restart deployment kube-experiment -n multivac

# Check rollout status
kubectl rollout status deployment kube-experiment -n multivac
```

## Troubleshooting

```bash
# Describe pod for events
kubectl describe pod -n multivac -l app=kube-experiment

# Get pod logs
kubectl logs -n multivac -l app=kube-experiment --tail=100

# Execute shell in pod
kubectl exec -it -n multivac <pod-name> -- /bin/sh

# Check service endpoints
kubectl get endpoints kube-experiment -n multivac
```

## Clean Up

To remove all resources:

```bash
kubectl delete -f k8s/
```

Or delete the namespace (removes everything):

```bash
kubectl delete namespace multivac
```

## Configuration

### Deployment Configuration
- **Replicas**: 2
- **Memory Request**: 256Mi
- **Memory Limit**: 512Mi
- **CPU Request**: 250m
- **CPU Limit**: 500m
- **Liveness Probe**: /actuator/health/liveness (checks every 10s)
- **Readiness Probe**: /actuator/health/readiness (checks every 5s)

### Service Configuration
- **Type**: ClusterIP
- **Port**: 80 → 8080 (targetPort)

### Istio Gateway Configuration
- **Protocols**: HTTP (port 80) + HTTPS (port 443)
- **TLS**: SIMPLE mode, credential `kube-experiment-tls`
- **Host**: kube-experiment.multivac.local

### Istio VirtualService Configuration
- **Gateway**: kube-experiment-gateway
- **Routing**: all traffic (`/`) → kube-experiment service on port 80

