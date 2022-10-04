# Version 5.2
# Status: Stable
# Documentation: https://github.com/Ankit404butfound/PyWhatKit/wiki
# Report Bugs and Feature Requests here: https://github.com/Ankit404butfound/PyWhatKit/issues
# For further Information, Join our Discord: https://discord.gg/62Yf5mushu

__VERSION__ = "Version 5.3 (Stable)"

from platform import system

from pywhatkit.misc import info, playonyt, search, show_history, web_screenshot

if system().lower() in ("darwin", "windows"):
    from pywhatkit.misc import take_screenshot

if system().lower() == "windows":
    from pywhatkit.remotekit import start_server
