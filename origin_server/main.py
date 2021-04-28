import os
from flask import Flask

IMAGE_URL_TEMPLATE = "https://picsum.photos/{}/{}"

TOTAL_IMAGES = int(os.environ["TOTAL_IMAGES"])
MOUNT_DIR = os.environ["MOUNT_DIR"]

import requests
import random

def generate_dimensions():
    sizes = [200, 300, 400, 500, 800, 1000, 1500]
    return random.choices(sizes, k=2)


for i in range(0, TOTAL_IMAGES):
    print(f"Fetching image no. {i + 1}")
    response = requests.get(IMAGE_URL_TEMPLATE.format(*generate_dimensions()))
    if response.status_code == 200:
        with open(f"{MOUNT_DIR}{i + 1}.jpg", 'wb') as f:
            f.write(response.content)
            
app = Flask(__name__, static_url_path="/", static_folder=MOUNT_DIR)

app.run()

