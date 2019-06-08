import math  # To calculate the WAV file content
import numpy as np  # To handle matrices
import cv2
from socket import socket

'''
source: https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f
'''


def process_image(size, img) -> np.array:
    img_arr = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Scale between 0 and 1
    img_arr -= np.min(img_arr)
    img_arr = img_arr / np.max(img_arr)

    if size[0] <= 0:
        size = img_arr.shape[0], size[1]
    if size[1] <= 0:
        size = size[0], img_arr.shape[1]

    # Resampling factor
    factor = size[0] / img_arr.shape[0], size[1] / img_arr.shape[1]
    if factor[0] <= 0:
        factor = 1, factor[1]
    if factor[1] <= 0:
        factor = factor[0], 1

    img_arr = cv2.resize(img_arr, (0, 0), fx=factor[1], fy=factor[0], interpolation=cv2.INTER_NEAREST)
    return img_arr


def gen_sound_from_image(img, duration, sample_rate, intensity_factor, min_freq, max_freq, rtp_socket: socket) -> None:
    max_frame: int = int(duration * sample_rate)
    max_intensity: int = 32767  # Defined by WAV

    step_size: int = 400  # Hz, each pixel's portion of the spectrum
    stepping_spectrum: int = int((max_freq - min_freq) / step_size)

    img_mat = process_image(size=(stepping_spectrum, max_frame), img=img)
    img_mat *= max_intensity  # To scale it to max WAV audio intensity
    buf: list = []

    for frame in range(max_frame):
        signal_value: float = 0.0
        count: int = 0

        for step in range(stepping_spectrum):
            intensity = img_mat[step, frame]
            if intensity < 0.1 * intensity_factor:
                continue

            # nextFreq is less than currentFreq
            current_freq = (step * step_size) + min_freq
            next_freq = ((step + 1) * step_size) + min_freq
            if next_freq - min_freq > max_freq:  # If we're at the end of the spectrum
                next_freq = max_freq

            for freq in range(current_freq, next_freq, 1000):  # substep of 1000 Hz is good
                signal_value += float(intensity * math.cos(freq * 2 * math.pi * float(frame) / float(sample_rate)))
                count += 1

        if count == 0:
            count = 1

        signal_value /= count
        buf.append(int(signal_value))

        # Send to streamer
        if len(buf) >= 1024:
            send_chunk(rtp_socket, buf)
            buf = []

    # If some data left send it again to streamer
    if len(buf) > 0:
        send_chunk(rtp_socket, buf)


def int16_to_bytes(data: list) -> bytes:
    result: list = []

    for e in data:
        first_byte: int = (e & int('0xFF00', 16)) >> 8
        second_byte: int = e & int('0x00FF', 16)
        result.append(second_byte)
        result.append(first_byte)

    return bytes(result)


def send_chunk(rtp_socket: socket, buf: []) -> None:
    data = int16_to_bytes(buf)
    rtp_socket.send(len(data).to_bytes(4, byteorder='big'))
    rtp_socket.send(data)
