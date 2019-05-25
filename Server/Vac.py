from multiprocessing import Pool
from socket import socket
import conversion
import numpy as np


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
        self.proc_pool.apply_async(func=conversion.gen_sound_from_image, args=args)
        return

    def start(self):
        self.stream.start_stream()
        return

    def shutdown(self):
        # stop stream
        self.stream.stop_stream()
        self.stream.close()
        return
