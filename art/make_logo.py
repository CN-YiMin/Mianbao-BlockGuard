from PIL import Image, ImageDraw

S = 4096
img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)


def sc(v):
    return int(v * S / 512)


# rounded dark tile
d.rounded_rectangle([sc(24), sc(24), sc(488), sc(488)], radius=sc(104),
                    fill=(15, 23, 42, 255), outline=(45, 212, 191, 255), width=sc(10))
d.rounded_rectangle([sc(48), sc(48), sc(464), sc(464)], radius=sc(84),
                    outline=(30, 41, 59, 255), width=sc(4))

# terrain blocks at the bottom
for x, y, w, h, c in [
    (120, 372, 72, 44, (71, 85, 105, 255)),
    (190, 386, 64, 30, (100, 116, 139, 255)),
    (314, 386, 64, 30, (100, 116, 139, 255)),
    (352, 372, 72, 44, (71, 85, 105, 255)),
]:
    d.rounded_rectangle([sc(x), sc(y), sc(x + w), sc(y + h)], radius=sc(8), fill=c)

# shield
shield = [(256, 88), (382, 142), (382, 286), (256, 410), (130, 286), (130, 142)]
d.polygon([(sc(x), sc(y)) for x, y in shield], fill=(19, 78, 74, 255))
d.line([(sc(x), sc(y)) for x, y in shield + [shield[0]]],
       fill=(94, 234, 212, 255), width=sc(12), joint='curve')

# missile body
d.rounded_rectangle([sc(232), sc(178), sc(280), sc(330)], radius=sc(12),
                    fill=(203, 213, 225, 255), outline=(148, 163, 184, 255), width=sc(4))
# nose cone
d.polygon([(sc(232), sc(180)), (sc(280), sc(180)), (sc(256), sc(120))],
          fill=(248, 113, 113, 255))
# fins
d.polygon([(sc(232), sc(286)), (sc(198), sc(330)), (sc(232), sc(330))],
          fill=(148, 163, 184, 255))
d.polygon([(sc(280), sc(286)), (sc(314), sc(330)), (sc(280), sc(330))],
          fill=(148, 163, 184, 255))
# window
d.ellipse([sc(244), sc(206), sc(268), sc(230)], fill=(45, 212, 191, 255),
          outline=(15, 23, 42, 255), width=sc(3))

# blocked slash
d.line([(sc(150), sc(352)), (sc(362), sc(148))], fill=(248, 113, 113, 255), width=sc(26))
for cx, cy in [(150, 352), (362, 148)]:
    d.ellipse([sc(cx - 13), sc(cy - 13), sc(cx + 13), sc(cy + 13)], fill=(248, 113, 113, 255))

# corner accents
for cx, cy in [(74, 74), (438, 74), (74, 438), (438, 438)]:
    d.ellipse([sc(cx - 6), sc(cy - 6), sc(cx + 6), sc(cy + 6)], fill=(45, 212, 191, 255))

img = img.resize((512, 512), Image.LANCZOS)
img.save(r'mianbao-blockguard/art/logo.png')
print('saved', img.size)
