#!/usr/bin/env python3
"""Deterministic Library-only crop PNGs from unmodified source art. Python stdlib."""
from hashlib import sha256
from pathlib import Path
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1]
RESOURCE = ROOT / 'app/src/main/res/drawable-nodpi'
OUTPUT = ROOT / 'app/build/generated/rc65/res/drawable-nodpi'
SPEC = {
    'conservation': ('likas_generated_conservation.png', 4,
        '749b6b0ba720448b76a52ade7e9fd4b1904be4d48b040e40d0642f3cc22ce60e'),
    'habitat_links': ('likas_generated_habitat_links.png', 4,
        '7a618c2f2f20bedc62b068ed96ebd6e1f059c45b0c6b20e3dea89f05fb92321e'),
    'photos': ('likas_approved_camera.png', 24,
        '07b46b321121a918f3c29c109b4612b72ca9fa997aa259485ae674901f40f275'),
    'sources': ('likas_approved_reports.png', 24,
        '1a3a09b98441ab0fccfc3f27ac69222161784186496880a5f92a17c2571a1c09'),
}
MAGIC=b'\x89PNG\r\n\x1a\n'

def chunk(kind, data):
    return (struct.pack('>I',len(data))+kind+data+
            struct.pack('>I',zlib.crc32(kind+data)&0xffffffff))

def read_png(raw):
    assert raw.startswith(MAGIC), 'Not PNG'
    pos=8
    blocks={}
    while pos<len(raw):
        n,=struct.unpack_from('>I',raw,pos)
        tag=raw[pos+4:pos+8]
        payload=raw[pos+8:pos+8+n]
        assert zlib.crc32(tag+payload)&0xffffffff==struct.unpack_from('>I',raw,pos+8+n)[0]
        blocks.setdefault(tag,[]).append(payload)
        pos+=n+12
        if tag==b'IEND':break
    assert pos==len(raw), 'Trailing PNG data'
    w,h,bits,kind,compression,filtration,interlace=struct.unpack('>IIBBBBB',blocks[b'IHDR'][0])
    assert bits==8 and kind in (2,3) and compression==filtration==interlace==0
    bpp=3 if kind==2 else 1
    stream=zlib.decompress(b''.join(blocks[b'IDAT']))
    stride=w*bpp
    assert len(stream)==h*(stride+1)
    rows=[]
    pos=0
    previous=bytearray(stride)
    for _ in range(h):
        filter_kind=stream[pos];pos+=1
        row=bytearray(stream[pos:pos+stride]);pos+=stride
        assert filter_kind in range(5)
        for i in range(stride):
            a=row[i-bpp] if i>=bpp else 0
            b=previous[i]
            c=previous[i-bpp] if i>=bpp else 0
            if filter_kind==1:row[i]=(row[i]+a)&255
            elif filter_kind==2:row[i]=(row[i]+b)&255
            elif filter_kind==3:row[i]=(row[i]+((a+b)//2))&255
            elif filter_kind==4:
                predictor=a+b-c
                da,db,dc=abs(predictor-a),abs(predictor-b),abs(predictor-c)
                row[i]=(row[i]+(a if da<=db and da<=dc else b if db<=dc else c))&255
        rows.append(bytes(row))
        previous=row
    return w,h,bpp,kind,rows,blocks

def make_display_png(raw, crop, target=256):
    width,height,bpp,color,rows,blocks=read_png(raw)
    assert width==height and width>crop*2
    span=width-2*crop
    data=bytearray()
    for oy in range(target):
        src=rows[crop+(oy*span//target)]
        data.append(0)
        for ox in range(target):
            ix=(crop+(ox*span//target))*bpp
            data.extend(src[ix:ix+bpp])
    header=struct.pack('>IIBBBBB',target,target,8,color,0,0,0)
    out=bytearray(MAGIC+chunk(b'IHDR',header))
    if color==3:
        out.extend(chunk(b'PLTE',blocks[b'PLTE'][0]))
        if b'tRNS' in blocks:out.extend(chunk(b'tRNS',blocks[b'tRNS'][0]))
    out.extend(chunk(b'IDAT',zlib.compress(bytes(data),9)))
    out.extend(chunk(b'IEND',b''))
    return bytes(out)

def generate(resource_dir=RESOURCE,output_dir=OUTPUT):
    output_dir.mkdir(parents=True,exist_ok=True)
    written=[]
    for label,(original,padding,hash_lock) in SPEC.items():
        original_bytes=(resource_dir/original).read_bytes()
        assert sha256(original_bytes).hexdigest()==hash_lock, original+' source hash changed'
        rendered=make_display_png(original_bytes,padding)
        dest=output_dir/('likas_library_'+label+'_display.png')
        if not dest.exists() or dest.read_bytes()!=rendered:
            dest.write_bytes(rendered)
        written.append((dest,sha256(rendered).hexdigest()))
    return written

if __name__=='__main__':
    for dest,digest in generate():print(dest,digest)
