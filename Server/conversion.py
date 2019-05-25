import math  # To calculate the WAV file content
import numpy as np  # To handle matrices
import cv2
from socket import socket
from multiprocessing import Pool

'''
source: https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f
'''


def process_image(size, img):
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


def gen_sound_from_image(img, duration, sample_rate, intensity_factor, min_freq, max_freq,
                         rtp_socket: socket):
    max_frame = int(duration * sample_rate)
    max_intensity = 32767  # Defined by WAV

    step_size = 400  # Hz, each pixel's portion of the spectrum
    stepping_spectrum = int((max_freq - min_freq) / step_size)

    img_mat = process_image(size=(stepping_spectrum, max_frame), img=img)

    # img_mat *= intensity_factor  # To lower/increase the image overall intensity
    # img_mat /= np.full(img_mat.shape, 255, img_mat.dtype)
    img_mat *= max_intensity  # To scale it to max WAV audio intensity
    buf = []

    for frame in range(max_frame):
        # end if
        signal_value, count = float(0), int(0)
        for step in range(stepping_spectrum):
            intensity = img_mat[step, frame]
            if intensity < 0.1 * intensity_factor:
                continue
            # end if
            # nextFreq is less than currentFreq
            current_freq = (step * step_size) + min_freq
            next_freq = ((step + 1) * step_size) + min_freq
            if next_freq - min_freq > max_freq:  # If we're at the end of the spectrum
                next_freq = max_freq
            # end if
            for freq in range(current_freq, next_freq, 1000):  # substep of 1000 Hz is good
                signal_value += float(intensity * math.cos(freq * 2 * math.pi * float(frame) / float(sample_rate)))
                count += 1
            # end for
        if count == 0:
            count = 1
        # end if
        signal_value /= count
        buf.append(int(signal_value))
        if len(buf) >= 1024:
            send_chunk(rtp_socket, buf)
            buf = []
    # end for
    if len(buf) > 0:
        send_chunk(rtp_socket, buf)


def int16_to_bytes(data: []):
    result: list = []

    for e in data:
        first_byte: int = (e & int('0xFF00', 16)) >> 8
        second_byte: int = e & int('0x00FF', 16)
        result.append(second_byte)
        result.append(first_byte)

    return bytes(result)


def send_chunk(rtp_socket: socket, buf: []):
    data = int16_to_bytes(buf)
    rtp_socket.send(len(data).to_bytes(4, byteorder='big'))
    rtp_socket.send(data)
    # print("+------------------------+")
    # print("| Sent sound with length : " + str(len(data)))
    # print("+------------------------+")


class Vac:
    ready = False
    sample_rate: int = 44100
    sample = np.array([])
    stream = None
    proc_pool: Pool = None
    rtp_socket: socket = None

    def __init__(self, rtp_socket: socket):
        self.rtp_socket = rtp_socket
        self.proc_pool = Pool(20)
        return

    def feed_image(self, img):
        duration = 0.25
        min_freq = 0
        max_freq = int(self.sample_rate / 2)
        intensity_factor = 1

        args = (
            img,
            duration,
            self.sample_rate,
            intensity_factor,
            min_freq,
            max_freq,
            self.rtp_socket,
        )
        self.proc_pool.apply_async(func=gen_sound_from_image, args=args)
        return

    def start(self):
        self.stream.start_stream()
        return

    def shutdown(self):
        # stop stream
        self.stream.stop_stream()
        self.stream.close()
        return
