import random
from locust import HttpUser, task, between

class GroceryClientUser(HttpUser):
    wait_time = between(0.5, 1.5)

    def on_start(self):
        self.auth_token = "Bearer mock-production-jwt-token-here"
        self.headers = {
            "Authorization": self.auth_token,
            "Content-Type": "application/json"
        }
        self.user_id = random.randint(1000, 9999)

    @task(1)
    def view_inventory(self):
        self.client.get("http://host.docker.internal:8082/api/v1/inventory", headers=self.headers)

    @task(3)
    def place_order(self):
        order_payload = {
            "userId": self.user_id,
            "items": [
                {"productId": str(random.randint(1, 10)), "quantity": random.randint(1, 5)},
                {"productId": str(random.randint(11, 20)), "quantity": random.randint(1, 3)}
            ],
            "idempotencyKey": f"idemp-key-{self.user_id}-{random.randint(100000, 999999)}"
        }

        with self.client.post("http://host.docker.internal:8081/api/v1/orders", json=order_payload, headers=self.headers, catch_response=True) as response:
            if response.status_code == 201:
                response.success()
            elif response.status_code == 409:
                response.failure("Data conflict / Optimistic lock failure under load")
            else:
                response.failure(f"Unexpected error: {response.status_code}")