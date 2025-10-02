#!/usr/bin/env python3
"""
Fetch N unique random recipes from TheMealDB and save to JSON.

Usage:
  python db_init/theMealdbFetch.py --count 100 --out meals.json --api-key 1
"""

import argparse
import json
import time
import random
from typing import Dict, Any, List, Set, Optional

import requests
from tqdm import tqdm  # <-- add tqdm


RANDOM_URL_TEMPLATE = "https://www.themealdb.com/api/json/v1/{api_key}/random.php"


def fetch_random_meal(session: requests.Session, api_key: str, timeout: float = 10.0) -> Optional[Dict[str, Any]]:
    """Call /random.php once and return the meal dict, or None on failure/empty."""
    url = RANDOM_URL_TEMPLATE.format(api_key=api_key)
    try:
        resp = session.get(url, timeout=timeout)
        resp.raise_for_status()
        data = resp.json()
        meals = data.get("meals")
        if isinstance(meals, list) and meals:
            return meals[0]
        return None
    except (requests.RequestException, ValueError):
        return None


def backoff_sleep(attempt: int) -> None:
    """Exponential backoff with jitter."""
    base = min(2 ** attempt, 16)  # cap growth
    time.sleep(base * 0.2 + random.random() * 0.3)


def fetch_unique_random_meals(
    target_count: int,
    api_key: str = "1",
    polite_delay: float = 0.25,
    max_total_calls: int = 2000,
) -> List[Dict[str, Any]]:
    """
    Keep calling /random.php until we collect target_count unique meals
    or until max_total_calls is reached (to avoid infinite loops).
    Shows a tqdm progress bar.
    """
    session = requests.Session()
    session.headers.update({"User-Agent": "TheMealDB-fetcher/1.0 (+for academic use)"})

    unique_ids: Set[str] = set()
    meals: List[Dict[str, Any]] = []

    total_calls = 0
    consecutive_failures = 0

    with tqdm(total=target_count, desc="Fetching recipes", unit="meal") as pbar:
        while len(meals) < target_count and total_calls < max_total_calls:
            total_calls += 1

            meal = fetch_random_meal(session, api_key=api_key)

            if meal is None:
                consecutive_failures += 1
                backoff_sleep(min(consecutive_failures, 5))
                continue

            consecutive_failures = 0

            meal_id = str(meal.get("idMeal") or "")
            if not meal_id:
                # Skip malformed
                continue

            if meal_id in unique_ids:
                # Duplicate, just wait politely and continue
                time.sleep(polite_delay)
                continue

            unique_ids.add(meal_id)
            meals.append(meal)

            pbar.update(1)  # ✅ progress bar moves when we add a new unique recipe

            # polite delay to avoid hammering the API
            time.sleep(polite_delay)

    return meals


def main():
    parser = argparse.ArgumentParser(description="Fetch unique random recipes from TheMealDB.")
    parser.add_argument("--count", type=int, default=100, help="Number of unique recipes to fetch (default: 100).")
    parser.add_argument("--out", type=str, default="meals.json", help="Output JSON file path (default: meals.json).")
    parser.add_argument("--api-key", type=str, default="1", help="TheMealDB API key (default: '1' public demo key).")
    parser.add_argument("--delay", type=float, default=0.25, help="Delay (seconds) between calls (default: 0.25).")
    parser.add_argument("--max-calls", type=int, default=2000, help="Safety cap on total API calls (default: 2000).")
    args = parser.parse_args()

    target = max(1, args.count)

    print(f"Fetching {target} unique random recipes from TheMealDB (api_key={args.api_key})...")
    meals = fetch_unique_random_meals(
        target_count=target,
        api_key=args.api_key,
        polite_delay=args.delay,
        max_total_calls=args.max_calls,
    )

    unique = len(meals)
    if unique < target:
        print(f"\n⚠️ Warning: collected only {unique}/{target} unique recipes. Saving what we have.")

    # Save pretty JSON
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(
            {
                "source": "TheMealDB",
                "api_key_used": args.api_key if args.api_key != "1" else "public-demo",
                "count_requested": target,
                "count_collected": unique,
                "meals": meals,
            },
            f,
            ensure_ascii=False,
            indent=2,
        )

    print(f"\n✅ Saved {unique} recipes to {args.out}")


if __name__ == "__main__":
    main()
