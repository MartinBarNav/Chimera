# THIS IS TEMPORARY! THIS FILE WILL BE REMOVED WITH AUTOMATED APK-SIGNING!
# I just copied this file from project enzyme. It's a temporary development tool, please, please, please remove asap thx

build/apk/signed/mod.apk:
	zipalign -f 4 build/apk/mod.apk build/apk/signed/mod.apk
	apksigner sign --ks chimera.keystore --ks-pass file:pass.txt --v1-signing-enabled true --v2-signing-enabled true build/apk/signed/mod.apk
	@echo -e '\033[1;32mAPK signed successfully!\033[0m'