# syntax=docker/dockerfile:1

# Build the application from source
FROM golang:1.22-alpine AS build

# Set destination for COPY
WORKDIR /app

# Download Go modules
COPY go.mod ./
RUN go mod download

# Copy the source code. Note the slash at the end, as explained in
# https://docs.docker.com/reference/dockerfile/#copy
COPY *.go ./

# Build
RUN CGO_ENABLED=0 GOOS=linux go build -o /trailcompass

# Run the tests in the container
FROM build-stage AS test
RUN go test -v ./...

# Deploy the application binary into a lean image
FROM alpine:3.21 AS release

WORKDIR /server
COPY --from=build /trailcompass /server/trailcompass

EXPOSE 8080

# Run
CMD ["/server/trailcompass"]