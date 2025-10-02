from rapidfuzz import fuzz
from typing import List, Dict, Any, Tuple
import re

_WORD_RE = re.compile(r"[a-z0-9]+")

def tokens(s: str) -> List[str]:
    s = (s or "").lower()
    return _WORD_RE.findall(s)

def prefixes(token: str, max_len: int = 10) -> List[str]:
    max_len = max(1, max_len)
    return [token[:i] for i in range(1, min(len(token), max_len) + 1)]

def make_keywords(name: str, category: str = "", area: str = "", tags: List[str] = None) -> List[str]:
    fields = [name, category or "", area or "", " ".join(tags or [])]
    toks: List[str] = []
    for f in fields:
        for t in tokens(f):
            toks.append(t)
            toks.extend(prefixes(t))
    # dedupe and cap
    out, seen = [], set()
    for t in toks:
        if t and t not in seen:
            seen.add(t)
            out.append(t)
        if len(out) >= 300:
            break
    return out


def fuzzy_filter(items: List[Dict[str, Any]], query: str, score_cutoff: int = 60, limit: int = 20) -> List[Dict[str, Any]]:
    """
    Apply fuzzy filtering on a list of recipe dicts with improved relevance scoring.
    
    Args:
        items: List of recipe dictionaries
        query: Search query string
        score_cutoff: Minimum similarity score (0-100)
        limit: Maximum number of results to return
    
    Returns:
        List of recipes sorted by relevance score (best matches first)
    """
    if not query or not query.strip():
        return items[:limit]
    
    query_terms = query.strip().lower().split()
    results = []
    
    for item in items:
        search_keywords = [kw.lower() for kw in item.get("searchKeywords", [])]
        
        total_score = 0
        matched_terms = 0
        
        for term in query_terms:
            keyword_scores = [fuzz.partial_ratio(term, kw) for kw in search_keywords]
            best_score = max(keyword_scores) if keyword_scores else 0
            # Bonus for exact matches
            if best_score >= 90:
                best_score += 10  
            
            total_score += best_score
            # Count how many terms matched well
            if best_score >= 70:
                matched_terms += 1
        
        if matched_terms > 0:
            final_score = (total_score / len(query_terms)) + (matched_terms * 5)
        else:
            final_score = total_score / len(query_terms)
        
        final_score = min(100, final_score)
        
        if final_score >= score_cutoff:
            results.append((final_score, item))
    
    results.sort(key=lambda x: x[0], reverse=True)
    
    return [item for _, item in results][:limit]

def fuzzy_search_with_scores(items: List[Dict[str, Any]], query: str, score_cutoff: int = 60, limit: int = 20) -> List[Tuple[int, Dict[str, Any]]]:
    """
    Fuzzy search that returns scores with items for debugging.
    
    Returns:
        List of tuples (score, recipe) sorted by score
    """
    if not query or not query.strip():
        return [(100, item) for item in items[:limit]]
    
    query_terms = query.strip().lower().split()
    results = []
    
    for item in items:
        search_keywords = [kw.lower() for kw in item.get("searchKeywords", [])]
        
        total_score = 0
        matched_terms = 0
        
        for term in query_terms:
            keyword_scores = [fuzz.partial_ratio(term, kw) for kw in search_keywords]
            best_score = max(keyword_scores) if keyword_scores else 0
            
            if best_score >= 90:
                best_score += 10
            
            total_score += best_score
            
            if best_score >= 70:
                matched_terms += 1
        
        if matched_terms > 0:
            final_score = (total_score / len(query_terms)) + (matched_terms * 5)
        else:
            final_score = total_score / len(query_terms)
        
        final_score = min(100, final_score)
        
        if final_score >= score_cutoff:
            results.append((final_score, item))
    
    results.sort(key=lambda x: x[0], reverse=True)
    return results[:limit]

def fuzzy_search_user(items, query: str, limit):
    if not query or not query.strip():
        return items[:limit]
    query_terms = query.strip().lower().split()
    results = []
    
    for item in items:
        search_keywords = [kw.lower() for kw in item.get("searchKeywords", [])]
        
        total_score = 0
        matched_terms = 0
        
        for term in query_terms:
            #  exact word matching 
            if term in search_keywords:
                best_score = 100  
            else:
                # only use fuzzy for partial word matches 
                keyword_scores = [fuzz.ratio(term, kw) for kw in search_keywords]
                best_score = max(keyword_scores) if keyword_scores else 0
                if best_score < 85:
                    best_score = 0
            
            if best_score >= 90:
                best_score += 10
            total_score += best_score
            if best_score >= 70:
                matched_terms += 1
        
        if matched_terms > 0:
            final_score = (total_score / len(query_terms)) + (matched_terms * 5)
        else:
            final_score = total_score / len(query_terms)
        final_score = min(100, final_score)
        
        if final_score >= 80:
            results.append((final_score, item))
    
    results.sort(key=lambda x: x[0], reverse=True)
    return [item for _, item in results][:limit]