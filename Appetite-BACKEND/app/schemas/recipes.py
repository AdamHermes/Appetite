from pydantic import BaseModel, Field, HttpUrl
from typing import Optional, List

class Ingredient(BaseModel):
    ingredient: str = Field(..., description="Ingredient name, e.g. 'Sugar'")
    measure: Optional[str] = Field(None, description="Free-form amount, e.g. '2 tbsp'")

class RecipeCreate(BaseModel):
    name: str
    category: Optional[str] = None
    area: Optional[str] = None
    minutes: int = 0
    ratingAvg: Optional[float] = None
    tags: Optional[List[str]] = None
    thumbnail: Optional[str] = None
    ingredients: List[Ingredient] = []   # now only ingredient+measure
    steps: List[str] = []
    youtubeUrl: Optional[str] = None
    source: Optional[str] = None
