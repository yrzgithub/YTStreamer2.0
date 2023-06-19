from pafy import new
from youtubesearchpython import VideosSearch, Suggestions
from bs4 import BeautifulSoup
from requests import get
import pywhatkit

default_url = "https://www.youtube.com/watch?v="


def search(keywords):
    results = VideosSearch(keywords).result()["result"]

    res = []

    for result in results:
        url = default_url +  result["id"]
        title = result["title"]
        duration = result["duration"]
        published = result["publishedTime"]
        thumb = result["thumbnails"][0]["url"]
        uploader = result["channel"]["name"]
        uploader_icon = result["channel"]["thumbnails"][0]["url"]
        views = result["viewCount"]["short"]

        res.append({"url":url,"title":title,"duration":duration,"published_time":published,"thumb":thumb,"uploader":uploader,"uploader_icon":uploader_icon,"views":views})

    return res

def get_stream_and_thumb(source,query=True):
    if query:
        url = pywhatkit.playonyt(source,open_video = False).split("\\\\")[0].strip()
    else:
        url = source

    try:
        pafy_obj = new(url,basic=False)
    except:
        return

    audio = pafy_obj.getbestaudio()
    stream_url = audio.url
    thumb = pafy_obj.getbestthumb()
    title = pafy_obj.title
    duration = pafy_obj.duration
    return {"stream":stream_url,"thumb":thumb,"title":title,"duration":duration,"url":url}


def stream(url):
    pafy_obj = new(url,basic=False)
    audio = pafy_obj.getbestaudio()
    stream_url = audio.url
    return stream_url

def suggest(query):
    return Suggestions(region="IN").get(query)["result"]

def lyrics(query):
    url = f"https://www.google.com/search?query={query}"
    browser = BeautifulSoup(get(url).text)
    scrapped = []
    links = browser.find_all("a",href=True)
    for lin in links:
        href = lin["href"]
        if href.startswith("/url?q=") and "&" in href and "youtube.com" not in href and "google.com" not in href:
            rec = href.lstrip("/url?q=").split("&")[0]
            scrapped.append(rec)
    return scrapped

