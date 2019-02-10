#!/usr/bin/python
# coding: utf8
import time
import json
import sys
import requests
import fileinput
import argparse

from PIL import Image
from PIL import ImageDraw

from Adafruit_LED_Backpack import Matrix8x8

# Create display instance on default I2C address (0x70) and bus number.
display = Matrix8x8.Matrix8x8()

# Alternatively, create a display with a specific I2C address and/or bus.
# display = Matrix8x8.Matrix8x8(address=0x74, busnum=1)

# On BeagleBone, try busnum=2 if IOError occurs with busnum=1
# display = Matrix8x8.Matrix8x8(address=0x74, busnum=2)

# Initialize the display. Must be called once before using the display.
display.begin()

# Clear the display buffer.
display.clear()

numbers = [0xe040404040406040,
           0xf810204080808870,
           0x7088808060808870,
           0x8080f88890a0c080,
           0x70888080780808f8,
           0x7088888878088870,
           0x10101020408080f8,
           0x7088888870888870,
           0x708880f088888870,
           0x7088888888888870
           ]

signs = [
    0x0303030303030303,  # in
    0x0705070000000000,  # out
    0x00040a1120408000,  # check
    0x6666006666666600  # exclamation
]


def drawBitmap(draw, bitmap):
    for x in range(0, 8):
        row = (bitmap >> x * 8) & 0xFF
        for y in range(0, 8):
            if ((row & (1 << y)) != 0):
                draw.point((7 - x, y), fill=255)


def drawCount(number, inbound):
    # First create an 8x8 1 bit color image.
    image = Image.new('1', (8, 8))
    # Then create a draw instance.
    draw = ImageDraw.Draw(image)

    drawBitmap(draw, numbers[number - 1])
    if (inbound):
        drawBitmap(draw, signs[0])

    # Draw the image on the display buffer.
    display.set_image(image)

    # Draw the buffer to the display hardware.
    display.write_display()


def drawIcon(number):
    # First create an 8x8 1 bit color image.
    image = Image.new('1', (8, 8))
    # Then create a draw instance.
    draw = ImageDraw.Draw(image)

    drawBitmap(draw, signs[number])
    display.set_image(image)
    display.write_display()


def clearDisplay():
    image = Image.new('1', (8, 8))
    display.set_image(image)
    display.write_display()


def updateDisplay(count, inbound):
    drawCount(count, inbound)


parser = argparse.ArgumentParser(description='My Little Keller CLI.')
parser.add_argument('hostAndPath', metavar='hostAndPath',
                   help='Host and Path of the web application, e.g. host/mount for http://host/mount')

args = parser.parse_args()

print("My Little Keller: Ready")
sys.stdout.write(">")
sys.stdout.flush()

host = args.hostAndPath
inbound = True
count = 1

updateDisplay(count, inbound)

try:
    for line in fileinput.input():
        try:
            if ("!" in line):
                if ("count" in line):
                    print(line)
                    commandmarker = line.index("!")
                    if ("count:" in line):
                        count = int(line[6:commandmarker])
                        updateDisplay(count, inbound)
                    if ("count;" in line):
                        count = int(line[6:commandmarker])
                        updateDisplay(count, inbound)
                    if ("countÖ" in line):
                        count = int(line[7:commandmarker])
                        updateDisplay(count, inbound)
                if ("inbound" in line):
                    inbound = True
                    updateDisplay(count, inbound)
                if ("outbound" in line):
                    inbound = False
                    updateDisplay(count, inbound)
            else:
                ean = line.strip()
                if (inbound):
                    r = requests.post('http://{}/api/events/inbound'.format(host), json={"ean": ean, "count": count})
                else:
                    r = requests.post('http://{}/api/events/outbound'.format(host), json={"ean": ean, "count": count})

                if (r.status_code > 200):
                    raise ValueError

                print("Submitted EAN {}".format(ean))
                drawIcon(2)
                count = 1
                time.sleep(1)
                updateDisplay(count, inbound)
                "foo"
        except Exception as err:
            print("Unexpected error:", sys.exc_info()[0])
            drawIcon(3)
            time.sleep(1)
            updateDisplay(count, inbound)

except KeyboardInterrupt:
    clearDisplay()
    exit(0)
