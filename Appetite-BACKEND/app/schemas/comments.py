from pydantic import BaseModel

class CommentIn(BaseModel):
    text: str