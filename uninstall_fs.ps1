# удалить AG из ТСД/Эмулятора и стереть его файлы
adb shell pm uninstall com.example.admin.filesystem
adb shell rm -r /sdcard/MyDir