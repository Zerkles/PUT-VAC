import math  # To calculate the WAV file content
import numpy as np  # To handle matrices
# WAV playing
import pyaudio
import scipy.ndimage  # To resample using nearest neighbour
import cv2

'''
Source:
https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f
'''


class Vac:
    # instantiate PyAudio (1)
    audio = pyaudio.PyAudio()
    ready = False
    sample_rate = int(44100)
    sample = np.array([])
    stream = None

    def playing_callback(self, in_data, frame_count, time_info, status):
        while not self.ready:
            pass
        return self.sample, pyaudio.paContinue

    def __init__(self):
        self.stream = self.audio.open(
            format=self.audio.get_format_from_width(2),
            channels=1,
            rate=self.sample_rate,
            output=True,
            stream_callback=self.playing_callback
        )
        return

    @staticmethod
    def process_image(size, img, highpass=False):
        img_arr = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        '''
        cv2.imshow('Input', img_arr)
    
        # ret, img_thresh = cv2.threshold(img_arr, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
        # img_thresh = cv2.adaptiveThreshold(img_arr, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)
    
        cv2.imshow('Output', img_thresh)
        cv2.waitKey()
        cv2.destroyAllWindows()
        '''

        # Scale between 0 and 1
        img_arr -= np.min(img_arr)
        img_arr = img_arr / np.max(img_arr)
        # Remove low pixel values (highpass filter)
        if highpass:
            remove_low_values = np.vectorize(lambda x: x if x > 0.5 else 0, otypes=[np.float])
            img_arr = remove_low_values(img_arr)

        if size[0] == 0:
            size = img_arr.shape[0], size[1]
        if size[1] == 0:
            size = size[0], img_arr.shape[1]
        resampling_factor = size[0] / img_arr.shape[0], size[1] / img_arr.shape[1]
        if resampling_factor[0] == 0:
            resampling_factor = 1, resampling_factor[1]
        if resampling_factor[1] == 0:
            resampling_factor = resampling_factor[0], 1

        # Order : 0=nearestNeighbour, 1:bilinear, 2:cubic etc...
        img_arr = scipy.ndimage.zoom(img_arr, resampling_factor, order=0)

        return img_arr

    def gen_sound_from_image(self, img, duration=5.0, sample_rate=44100.0, intensity_factor=1, min_freq=0,
                             max_freq=22050, invert=False, highpass=True):
        max_frame = int(duration * sample_rate)
        max_intensity = 32767  # Defined by WAV

        step_size = 400  # Hz, each pixel's portion of the spectrum
        stepping_spectrum = int((max_freq - min_freq) / step_size)

        img_mat = self.process_image(size=(stepping_spectrum, max_frame), img=img, highpass=highpass)
        if invert:
            img_mat = 1 - img_mat
        # end if

        img_mat *= intensity_factor  # To lower/increase the image overall intensity
        img_mat *= max_intensity  # To scale it to max WAV audio intensity

        print(img_mat)

        buf = []

        for frame in range(max_frame):
            if frame % 60 == 0:  # Only print once in a while
                print("Progress: ==> {:.2%}".format(frame / max_frame), end="\r")
            # end if
            signal_value, count = 0, 0
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
                    signal_value += intensity * math.cos(freq * 2 * math.pi * float(frame) / float(sample_rate))
                    count += 1
                # end for
            if count == 0:
                count = 1
            # end if
            signal_value /= count

            buf.append(int(signal_value))
        # end for
        # print("\nProgress: ==> 100%")
        return buf

    def feed_image(self, img):
        duration = 0.03333
        min_freq = 0
        max_freq = int(self.sample_rate / 2)
        intensity_factor = 1
        invert = False
        highpass = True

        buf = self.gen_sound_from_image(
            img=img,
            duration=duration,
            sample_rate=self.sample_rate,
            min_freq=min_freq,
            max_freq=max_freq,
            invert=invert,
            intensity_factor=intensity_factor,
            highpass=highpass
        )

        self.sample = np.array(buf)
        if not self.ready:
            self.ready = True
        return

    def start(self):
        self.stream.start_stream()
        return

    def shutdown(self):
        # stop stream (4)
        self.stream.stop_stream()
        self.stream.close()

        # close PyAudio (5)
        self.audio.terminate()
        return
