#!/usr/bin/env python3

# pip3 install osxphotos
# brew install imagemagick jpeg

import osxphotos
import subprocess
from datetime import datetime, timezone
from pathlib import Path
import os.path

# convert heic to jpg and unify names to *.jpg
def convert(p, file_name):
    if (file_name.lower().endswith("heic")):
        to = file_name.lower().replace(".heic", ".jpg")
        print(f" -> heic to jpg ({to})", end="", flush=True)
        subprocess.run(["convert", file_name, to], check=True)
        return to
    elif (file_name.lower().endswith("jpeg")):
        to = file_name.lower().replace(".jpeg", ".jpg")
        print(f" -> jpeg to jpg ({to})", end="", flush=True)
        subprocess.run(["mv", file_name, to], check=True)
        return to
    else:
        return file_name

# adjust rotation for slideshow
def rotate_with_jpegtran(file_name, angle):
    tmp_name = f"{file_name}.tmp"
    print(f" -> rotate by {angle}", end="", flush=True)
    subprocess.run(["jpegtran", "-rotate", angle, "-outfile", tmp_name, file_name], check=True)
    subprocess.run(["mv", tmp_name, file_name])

# adjust rotation for slideshow
def rotate(p, file_name):
    if p.orientation == 8:
        rotate_with_jpegtran(file_name, "270")
    elif p.orientation == 3:
        rotate_with_jpegtran(file_name, "180")
    elif p.orientation == 6:
        rotate_with_jpegtran(file_name, "90")
    return file_name

# copy file to real target
def copy_to_target(p, file_name, target_path):
    d = p.date
    date_dir = f"{target_path}/{d.year:04}/{d.month:02}/{d.year:04}-{d.month:02}-{d.day:02}"
    Path(date_dir).mkdir(
        parents=True,
        exist_ok=True)
    output_file = f"{date_dir}/{os.path.basename(file_name)}"
    print(f" -> copy to {output_file}", end="")
    subprocess.run(["cp", file_name, output_file], check=True)
    return output_file

def process_slideshow(photo, working_path, target_path):
    Path(working_path).mkdir(
        parents=True,
        exist_ok=True)
    Path(target_path).mkdir(
        parents=True,
        exist_ok=True)

    print(f" -> Working on {photo.path}", end="", flush=True)
    if photo.favorite:
        try:
            file_name = photo.export(working_path,
                                     edited=photo.hasadjustments,
                                     increment=False,
                                     overwrite=False)[0]
            file_name = convert(photo, file_name)
            # slideshow cannot handle exif rotation -> rotate now
            file_name = rotate(photo, file_name)
            copy_to_target(photo, file_name, target_path)
            print(f" -> done")
            return file_name
        except FileExistsError:
            print(f" -> Already done")
            return None
    else:
        print(f" -> skipped")
        return None

def process_gphoto(photo, from_date, working_path, target_path):
    Path(working_path).mkdir(
        parents=True,
        exist_ok=True)
    Path(target_path).mkdir(
        parents=True,
        exist_ok=True)
    
    print(f" -> Working on {photo.path}", end="", flush=True)
    if photo.date > from_date and photo.favorite:
        try:
            # export and convert to jpg for all
            file_name = photo.export(working_path,
                                     edited=photo.hasadjustments,
                                     increment=False,
                                     overwrite=False)[0]
            file_name = convert(photo, file_name)

            # gphotos (no rotation needed)
            copy_to_target(photo, file_name, target_path)


            print(f" -> done")
            return file_name
        except FileExistsError:
            print(f" -> Already done")
            return None
    else:
        print(f" -> skipped")
        return None

if __name__ == "__main__":
    from_date = datetime.fromisoformat("2020-12-19T00:00+00:00")
    gphotos_path = "/Users/monica/tmp/imagelib-gphotos"
    slideshow_path = "/Users/monica/tmp/imagelib-slideshow"
    print(f"Exporting favorites newer than {from_date} to {gphotos_path} and {slideshow_path}")
    db = osxphotos.PhotosDB("TestDB.photoslibrary")
    photos = db.photos(movies=False)
    for num, photo in enumerate(photos, start=1):
        print(f"{(num/len(photos)*100):.0f}%", end="")
        process_gphoto(photo, from_date, f"{gphotos_path}-tmp", gphotos_path)
        process_slideshow(photo, f"{slideshow_path}-tmp", slideshow_path)
