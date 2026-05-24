# -*- coding: utf-8 -*-
"""Corrige mojibake comum em index.html (UTF-8 lido como CP1252)."""
from pathlib import Path
import re

p = Path(__file__).resolve().parents[1] / "src/main/resources/static/index.html"
t = p.read_text(encoding="utf-8-sig")

pairs = [
    ("Ã§Ãµes", "ções"),
    ("Ã§Ã£o", "ção"),
    ("Ã§Ãµ", "çõ"),
    ("Ã§", "ç"),
    ("Ã£", "ã"),
    ("Ã¡", "á"),
    ("Ã©", "é"),
    ("Ã­", "í"),
    ("Ã³", "ó"),
    ("Ãµ", "õ"),
    ("Ãº", "ú"),
    ("Ã‰", "É"),
    ("Ãš", "Ú"),
    ("Ã ", "à"),
    ("Ã¢", "â"),
    ("Ãª", "ê"),
    ("Ã´", "ô"),
    ("Â·", "·"),
    ("â€”", "—"),
    ("\u2013", "—"),
    ("â†’", "→"),
    ("âš™ï¸\x8f", "⚙️"),
    ("âš™ï¸", "⚙️"),
    ("ðŸ”´", "🔴"),
]
for a, b in pairs:
    t = t.replace(a, b)

# Em dash solto
t = t.replace("â€", "—")

p.write_text(t, encoding="utf-8", newline="\n")
suspicious = len(re.findall(r"Ã.|â\w|ï¸", t))
print("written", p)
print("suspicious tokens left:", suspicious)
