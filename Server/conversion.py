import math  # To calculate the WAV file content
import numpy as np  # To handle matrices
# WAV playing
import pyaudio
import scipy.ndimage  # To resample using nearest neighbour
from PIL import Image  # To open the input image and convert it to greyscale
import cv2

# instantiate PyAudio (1)
p = pyaudio.PyAudio()

ready = False
sample = np.array([])


def playing_callback(in_data, frame_count, time_info, status):
    global sample
    global ready
    while not ready:
        pass
    data = sample
    return data, pyaudio.paContinue


stream = p.open(
    format=p.get_format_from_width(2),
    channels=1,
    rate=44100,
    output=True,
    stream_callback=playing_callback
)

# open stream using callback (3)

'''
Source:
https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f
'''


def load_picture(size, cv2_im, contrast=True, highpass=False, verbose=1):
    cv2_im = cv2.cvtColor(cv2_im, cv2.COLOR_BGR2RGB)
    img = Image.fromarray(cv2_im)
    img = img.convert("L")
    # img = img.resize(size) # DO NOT DO THAT OR THE PC WILL CRASH

    img_arr = np.array(img)
    img_arr = np.flip(img_arr, axis=0)
    # if verbose:
    #    print("Image original size: ", img_arr.shape)

    # Increase the contrast of the image
    if contrast:
        img_arr = 1 / (img_arr + 10 ** 15.2)  # Now only god knows how this works but it does
    else:
        img_arr = 1 - img_arr
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


def gen_sound_from_image(img, duration=5.0, sample_rate=44100.0, intensity_factor=1, min_freq=0,
                         max_freq=22000, invert=False, contrast=True, highpass=True, verbose=False):
    max_frame = int(duration * sample_rate)
    max_intensity = 32767  # Defined by WAV

    step_size = 400  # Hz, each pixel's portion of the spectrum
    stepping_spectrum = int((max_freq - min_freq) / step_size)

    img_mat = load_picture(size=(stepping_spectrum, max_frame), cv2_im=img, contrast=contrast, highpass=highpass,
                           verbose=verbose)
    if invert:
        img_mat = 1 - img_mat
    # end if

    img_mat *= intensity_factor  # To lower/increase the image overall intensity
    img_mat *= max_intensity  # To scale it to max WAV audio intensity

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


def conversion_add_image(img):
    global sample
    global ready

    duration = 0.03333
    min_freq = 0
    max_freq = 22050
    sample_rate = 44100
    intensity_factor = 1
    invert = False
    contrast = True
    highpass = True
    verbose = False

    buf = gen_sound_from_image(
        img=img,
        duration=duration,
        sample_rate=sample_rate,
        min_freq=min_freq,
        max_freq=max_freq,
        contrast=contrast,
        invert=invert,
        intensity_factor=intensity_factor,
        highpass=highpass,
        verbose=verbose
    )

    sample = np.array(buf)
    if not ready:
        ready = True
    return


def start_conversion():
    stream.start_stream()
    return


def end_conversion():
    # stop stream (4)
    stream.stop_stream()
    stream.close()

    # close PyAudio (5)
    p.terminate()
    return
