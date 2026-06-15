import os

strategies = [
    ("Titolo", "brano1.getTitolo()", "brano2.getTitolo()"),
    ("Autore", "brano1.getAutore()", "brano2.getAutore()"),
    ("Anno", "Integer.valueOf(brano1.getAnno())", "Integer.valueOf(brano2.getAnno())"),
    ("Genere", 'brano1.getGenereEnum() != null ? brano1.getGenereEnum().getEtichetta() : ""', 'brano2.getGenereEnum() != null ? brano2.getGenereEnum().getEtichetta() : ""'),
    ("Tag", 'brano1.getTag() != null ? brano1.getTag().getEtichetta() : ""', 'brano2.getTag() != null ? brano2.getTag().getEtichetta() : ""')
]

base_dir = r"c:\Users\nello\Desktop\SadVsCode\ProgettoSAD-G16\src\main\java\com\musicplayer\strategy"

template = """package com.musicplayer.strategy;

import com.musicplayer.model.Brano;
import com.musicplayer.model.IBrano;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ordina{name}{direction} implements OrdinamentoStrategy {{

    @Override
    public void ordina(List<IBrano> brani) {{
        Comparator<IBrano> comparator = (b1, b2) -> {{
            if (!(b1 instanceof Brano brano1) || !(b2 instanceof Brano brano2)) {{
                return 0;
            }}

            int result = compare({val1}, {val2});
            return {sign}result;
        }};

        Collections.sort(brani, comparator);
    }}

    private int compare(Object o1, Object o2) {{
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        
        if (o1 instanceof String s1 && o2 instanceof String s2) {{
            return s1.compareToIgnoreCase(s2);
        }}
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) {{
            return i1.compareTo(i2);
        }}
        return 0;
    }}
}}
"""

for name, val1, val2 in strategies:
    for direction, sign in [("Asc", ""), ("Desc", "-")]:
        filename = os.path.join(base_dir, f"Ordina{name}{direction}.java")
        content = template.format(name=name, direction=direction, val1=val1, val2=val2, sign=sign)
        with open(filename, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Created {filename}")
