from flask import Flask, request, jsonify
from ultralytics import YOLO
import os
import cv2
import tempfile

app = Flask(__name__)

# 모델 로드 (서버 실행 시 1회만)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
WEIGHT = os.path.join(BASE_DIR, "best.pt")
model = YOLO(WEIGHT)

DISPOSAL = {
    "캔":      "캔 전용 수거함에 버려주세요.",
    "플라스틱":  "플라스틱 전용 수거함에 버려주세요.",
    "종이":    "종이류 전용 수거함에 버려주세요.",
    "비닐":    "비닐 전용 수거함에 버려주세요",
    "페트병":  "페트병 전용 수거함에 버려주세요",
    "유리병":  "유리 전용 수거함에 버려주세요",
    "스티로폼": "스티로폼 전용 수거함에 버려주세요"
}

# ===== 테스트용 루트 =====
@app.route("/", methods=["GET"])
def home():
    return jsonify({"status": "server is running"}), 200

# ===== 이미지 분석 =====
@app.route("/recycle/analyze", methods=["POST"])
def analyze_image():
    if 'image' not in request.files:
        return jsonify({"error": "이미지 파일이 필요합니다."}), 400

    image_file = request.files['image']
    if image_file.filename == "":
        return jsonify({"error": "파일 이름이 비어 있습니다."}), 400

    with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as temp:
        image_file.save(temp.name)
        img = cv2.imread(temp.name)

    if img is None:
        return jsonify({"error": "이미지를 읽을 수 없습니다."}), 400

    results = model(img, verbose=False)
    if len(results[0].boxes) == 0:
        return jsonify({"error": "객체를 감지하지 못했습니다."}), 400

    best = max(results[0].boxes, key=lambda b: float(b.conf[0]))
    cls_id = int(best.cls[0])
    confidence = float(best.conf[0])
    category = model.names[cls_id]
    disposal = DISPOSAL.get(category, "일반 쓰레기통에 버려주세요.")

    os.remove(temp.name)

    return jsonify({
        "category": category,
        "confidence": round(confidence, 4),
        "disposal_method": disposal
    })

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)