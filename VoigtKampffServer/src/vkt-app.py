from fastapi import FastAPI, Request, Form, HTTPException
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
import torch
from transformers import BertTokenizer, BertModel
import praw
from datetime import datetime
import numpy as np
from typing import Dict, Any, Optional
import logging
from training2 import BotDetectionModel, preprocess_user_data
from reddit import fetch_reddit_user
from fastapi import Query
from fastapi.responses import JSONResponse

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
templates = Jinja2Templates(directory="templates")

# Initialize Reddit API client
reddit = praw.Reddit(
    client_id="AvwC2-jU566H2PNuCZg02A",
    client_secret="B_dJ67X8Q28naG6xHbfCZ6UI1eNXng",
    user_agent="YourAppName (by u/YourRedditUsername)"
)

# Initialize model components
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
bert_model = BertModel.from_pretrained('bert-base-uncased')
tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')

try:
    model = BotDetectionModel(bert_model)
    model.load_state_dict(torch.load('bot_detection_model.pth', map_location=device))
    model.to(device)
    model.eval()
except Exception as e:
    logger.error(f"Failed to load model: {e}")
    model = None

def predict_bot_probability(user_data: Dict[str, Any]) -> float:
    """Calculate bot probability using the trained model."""
    try:
        text, numerical_features = preprocess_user_data(user_data)
        
        # Tokenize and prepare inputs
        encoding = tokenizer(
            text,
            max_length=512,
            padding='max_length',
            truncation=True,
            return_tensors='pt'
        )
        
        input_ids = encoding['input_ids'].to(device)
        attention_mask = encoding['attention_mask'].to(device)
        numerical_features = numerical_features.unsqueeze(0).to(device)
        
        # Make prediction
        with torch.no_grad():
            output = model(input_ids, attention_mask, numerical_features)
            return output.item()
    except Exception as e:
        logger.error(f"Prediction failed: {e}")
        raise


@app.get("/", response_class=HTMLResponse)
async def home(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

@app.post("/analyze", response_class=HTMLResponse)
async def analyze_user(request: Request, username: str = Form(...)):
    try:
        # Fetch user data
        user_data = fetch_reddit_user(username)
        if user_data is None:
            return templates.TemplateResponse(
                "result.html",
                {
                    "request": request,
                    "error": f"User '{username}' not found or is private",
                    "result": None
                }
            )
        
        # Calculate bot probability
        try:
            probability = predict_bot_probability(user_data)
        except Exception as e:
            logger.error(f"Error in prediction: {e}")
            probability = 0.5  # Default to uncertain
            
        # Calculate confidence metrics
        activity_score = min(1.0, (len(user_data.get("Latest Comments", [])) + 
                                 len(user_data.get("Latest Posts", []))) / 100)
        
        result = {
            "username": username,
            "bot_probability": probability,
            "classification": "Bot" if probability > 0.5 and activity_score > 0.3 else "Inconclusive" if activity_score < 0.3 else "Human",
            "confidence_score": activity_score,
            "key_indicators": {
                "activity_pattern": activity_score
            }
        }
        
        return templates.TemplateResponse(
            "result.html",
            {
                "request": request,
                "result": result,
                "error": None
            }
        )
        
    except Exception as e:
        logger.error(f"Error processing request: {e}")
        return templates.TemplateResponse(
            "result.html",
            {
                "request": request,
                "result": None,
                "error": str(e)
            }
        )

@app.get("/api/analyze", response_class=JSONResponse)
async def api_analyze_user(username: str = Query(..., description="Reddit username to analyze")):
    try:
        user_data = fetch_reddit_user(username)
        if user_data is None:
            raise HTTPException(status_code=404, detail=f"User '{username}' not found or is private")
        
        try:
            probability = predict_bot_probability(user_data)
        except Exception as e:
            logger.error(f"Prediction error: {e}")
            probability = 0.5  # fallback

        activity_score = min(1.0, (len(user_data.get("Latest Comments", [])) + 
                                   len(user_data.get("Latest Posts", []))) / 100)
        
        classification = (
            "Bot" if probability > 0.5 and activity_score > 0.3 else 
            "Inconclusive" if activity_score < 0.3 else 
            "Human"
        )
        
        result = {
            "username": username,
            "bot_probability": probability,
            "confidence_score": activity_score,
            "classification": classification,
            "key_indicators": {
                "activity_pattern": activity_score
            }
        }
        
        return JSONResponse(content=result)

    except HTTPException as he:
        raise he
    except Exception as e:
        logger.error(f"Unhandled API error: {e}")
        raise HTTPException(status_code=500, detail="Internal Server Error")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)