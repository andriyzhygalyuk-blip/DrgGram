import os, re, json, copy, shutil, subprocess
from PIL import Image, ImageDraw

run_no = int(os.environ.get("RUN_NO", "1") or "1")
new_vc = str(950000 + run_no)
if os.path.exists("gradle.properties"):
  with open("gradle.properties", "r", encoding="utf-8") as f: gp = f.read()
  gp = re.sub(r"APP_VERSION_CODE\s*=\s*\d+", f"APP_VERSION_CODE={new_vc}", gp)
  with open("gradle.properties", "w", encoding="utf-8") as f: f.write(gp)

wav_src = next((f for f in os.listdir(".") if f.lower().endswith((".wav", ".mp3", ".m4a", ".ogg"))), None)
for a_dir in ["TMessagesProj/assets", "TMessagesProj/src/main/assets", "TMessagesProj_AppStandalone/assets", "TMessagesProj_AppStandalone/src/main/assets", "TMessagesProj/src/main/res/raw"]:
  os.makedirs(a_dir, exist_ok=True)
  mp3_dst = os.path.join(a_dir, "drg_intro.mp3")
  if wav_src:
    try: subprocess.run(["ffmpeg", "-y", "-i", wav_src, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "192k", mp3_dst], check=True)
    except Exception: shutil.copyfile(wav_src, mp3_dst)

cyr = str.maketrans("сСіІїЇоОаАеЕуУрРмМхХкКвВнНтТ", "cciiiiooaaeeyyppmmxxkkbbhhtt")
def norm(s): return s.translate(cyr).lower().replace(" ", "").replace("_", "").replace("-", "")
def find_user_img(keys):
  for f in os.listdir("."):
    if not f.lower().endswith(".png"): continue
    clean = norm(f)
    for k in keys:
      if clean == k or clean == "drggram" + k or k.replace(".png","") in clean: return f
  return None

def extract_arrow_transparent(im):
  im = im.convert("RGBA").resize((432, 432), Image.LANCZOS)
  mask = Image.new("L", (432, 432), 0)
  ImageDraw.Draw(mask).rounded_rectangle([46, 46, 386, 386], radius=80, fill=255)
  px, mx = im.load(), mask.load()
  for y in range(432):
    for x in range(432):
      if mx[x, y] == 0: px[x, y] = (0, 0, 0, 0)
      else:
        r, g, b, a = px[x, y]
        br = max(r, g, b)
        if br <= 38: px[x, y] = (0, 0, 0, 0)
        elif br < 72: px[x, y] = (r, g, b, int((br - 38) * 255 / 34))
  out = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
  scaled = im.resize((330, 330), Image.LANCZOS)
  out.paste(scaled, (51, 51), scaled)
  return out

f_map = {1:["drggrammain.png","main.png"],2:["drggramcrimson.png","crimson.png"],3:["drggramsunset.png","sunset.png"],4:["drggramcyan.png","cyan.png"],5:["drggramglitch.png","glitch.png"],6:["drggramvintage.png","vintage.png","matrix.png"]}
arrows, used_files = {}, set()
for idx, keys in f_map.items():
  fn = find_user_img(keys)
  if fn and os.path.exists(fn):
    try:
      with Image.open(fn) as im_in:
        ar = extract_arrow_transparent(im_in.copy()); ar.load(); arrows[idx] = ar; used_files.add(fn)
    except Exception: pass
rem_pngs = [f for f in sorted(os.listdir(".")) if f.lower().endswith(".png") and f not in used_files]
for idx in range(1, 7):
  if idx not in arrows and rem_pngs:
    try:
      with Image.open(rem_pngs.pop(0)) as im_in:
        ar = extract_arrow_transparent(im_in.copy()); ar.load(); arrows[idx] = ar
    except Exception: pass
if 1 not in arrows and len(arrows) > 0: arrows[1] = next(iter(arrows.values()))

res_xx = "TMessagesProj/src/main/res/drawable-xxhdpi"
os.makedirs(res_xx, exist_ok=True)
Image.new("RGBA", (432, 432), (10, 10, 12, 255)).save(os.path.join(res_xx, "drg_bg_black.png"))
def make_full_ic(ar, sz=(192, 192)):
  full_ic = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
  ImageDraw.Draw(full_ic).rounded_rectangle([16, 16, 416, 416], radius=96, fill=(10, 10, 12, 255))
  full_ic.paste(ar, (0, 0), ar)
  return full_ic.resize(sz, Image.LANCZOS)
if 1 in arrows: make_full_ic(arrows[1], (432, 432)).save(os.path.join(res_xx, "drg_intro_clean.png"))

fg_names = {1:"icon_foreground_sa.png",2:"icon_2_foreground_sa.png",3:"icon_3_foreground_sa.png",4:"icon_4_foreground_sa.png",5:"icon_5_foreground_sa.png",6:"icon_6_foreground_sa.png"}
for idx in range(1, 7):
  ar = arrows.get(idx, arrows.get(1))
  if ar:
    ar.save(os.path.join(res_xx, f"drg_fg_{idx}.png"))
    ar.save(os.path.join(res_xx, fg_names[idx]))

black_shape = '<?xml version="1.0" encoding="utf-8"?><shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle"><solid android:color="#FF0A0A0C"/><corners android:radius="24dp"/></shape>'
def make_adaptive_xml(idx):
  return f'<?xml version="1.0" encoding="utf-8"?><adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/drg_bg_black"/><foreground android:drawable="@drawable/drg_fg_{idx}"/></adaptive-icon>'

for m_dir in ["TMessagesProj/src/main/res/mipmap-anydpi-v26", "TMessagesProj_AppStandalone/src/main/res/mipmap-anydpi-v26"]:
  os.makedirs(m_dir, exist_ok=True)
  for idx in range(1, 7):
    with open(os.path.join(m_dir, f"drg_ic_{idx}.xml"), "w", encoding="utf-8") as f: f.write(make_adaptive_xml(idx))
for m_dir in ["TMessagesProj/src/main/res/mipmap-xxhdpi", "TMessagesProj_AppStandalone/src/main/res/mipmap-xxhdpi"]:
  os.makedirs(m_dir, exist_ok=True)
  for idx in range(1, 7):
    ar = arrows.get(idx, arrows.get(1))
    if ar: make_full_ic(ar, (144, 144)).save(os.path.join(m_dir, f"drg_ic_{idx}.png"))

def get_dim(path):
  try:
    with Image.open(path) as im: return im.size
  except Exception: return (144, 144)

rev_fg = {v: k for k, v in fg_names.items()}
mip_png_map = {"ic_launcher.png":1,"ic_launcher_round.png":1,"ic_launcher_drg.png":1,"ic_launcher_sa.png":1,"icon_2_sa.png":2,"icon_3_sa.png":3,"icon_4_sa.png":4,"icon_5_sa.png":5,"icon_6_sa.png":6,"icon_2_round_sa.png":2,"icon_3_round_sa.png":3,"icon_4_round_sa.png":4,"icon_5_round_sa.png":5,"icon_6_round_sa.png":6,"icon_2_launcher.png":2,"icon_3_launcher.png":3,"icon_4_launcher.png":4,"icon_5_launcher.png":5,"icon_6_launcher.png":6,"icon_2_launcher_round.png":2,"icon_3_launcher_round.png":3,"icon_4_launcher_round.png":4,"icon_5_launcher_round.png":5,"icon_6_launcher_round.png":6}
mip_xml_map = {k.replace(".png",".xml"): v for k, v in mip_png_map.items()}
ctrl_slots = {"DEFAULT":1,"NOX":2,"PREMIUM":3,"AQUA":4,"TURBO":5,"VINTAGE":6}
man_slots = {"DefaultIcon":1,"NoxIcon":2,"PremiumIcon":3,"AquaIcon":4,"TurboIcon":5,"VintageIcon":6}

for root, _, files in os.walk("."):
  for f in files:
    p = os.path.join(root, f)
    try:
      if f.startswith("icon_") and "foreground" in f and f.endswith(".xml"): os.remove(p); continue
      if ("icon_" in f or "ic_launcher") and "background" in f and f.endswith(".xml"):
        with open(p, "w", encoding="utf-8") as file: file.write(black_shape); continue
      if f in mip_xml_map and "mipmap" in root:
        with open(p, "w", encoding="utf-8") as file: file.write(make_adaptive_xml(mip_xml_map[f])); continue
      if f in rev_fg:
        ar = arrows.get(rev_fg[f], arrows.get(1))
        if ar: ar.resize(get_dim(p), Image.LANCZOS).save(p)
      elif f in mip_png_map or f == "intro_tg_plane.png":
        ar = arrows.get(mip_png_map.get(f, 1), arrows.get(1))
        if ar: make_full_ic(ar, get_dim(p)).save(p)
      elif f == "notification.png" and 1 in arrows: arrows[1].resize(get_dim(p), Image.LANCZOS).save(p)
      elif "background" in f and f.endswith(".png") and ("icon_" in f or "ic_launcher" in f): Image.new("RGBA", get_dim(p), (10, 10, 12, 255)).save(p)
      if f == "LauncherIconController.java":
        with open(p, "r", encoding="utf-8") as file: s = file.read()
        for k, idx in ctrl_slots.items():
          s = re.sub(rf"{k}\s*\(\s*\"[^\"]+\"\s*,\s*[^,]+\s*,\s*[^,]+", f'{k}("DrgIcon{idx}", R.drawable.drg_bg_black, R.drawable.drg_fg_{idx}', s)
        s = s.replace(", true)", ", false)")
        with open(p, "w", encoding="utf-8") as file: file.write(s)
      elif f == "build.gradle":
        with open(p, "r", encoding="utf-8") as file: c = file.read()
        c = c.replace('applicationIdSuffix ".web"', 'applicationIdSuffix ".drggram"').replace("applicationIdSuffix '.web'", "applicationIdSuffix '.drggram'")
        if os.environ.get("FAST_BUILD") == "1" and "TMessagesProj" in p: c = re.sub(r"path\s+['\"]jni/CMakeLists\.txt['\"]", "// prebuilt", c)
        with open(p, "w", encoding="utf-8") as file: file.write(c)
      elif f == "google-services.json":
        with open(p, "r", encoding="utf-8") as file: gs = json.load(file)
        if "client" in gs and len(gs["client"]) > 0:
          for pkg in ["org.telegram.messenger.drggram", "org.telegram.messenger.drggram.web"]:
            nc = copy.deepcopy(gs["client"][0])
            nc["client_info"]["android_client_info"]["package_name"] = pkg
            gs["client"].append(nc)
        with open(p, "w", encoding="utf-8") as file: json.dump(gs, file, indent=2)
      elif f == "AndroidManifest.xml":
        with open(p, "r", encoding="utf-8") as file: m = file.read()
        m = m.replace("org.telegram.messenger.web.", "org.telegram.messenger.drggram.")
        if "largeHeap" not in m: m = m.replace("<application", '<application android:largeHeap="true"', 1)
        for al, idx in man_slots.items():
          def fix_al(mt, i=idx):
            t = re.sub(r'\s*android:(roundIcon|icon|name)="[^"]*"', '', mt.group(0))
            return t[:-1] + f' android:name="org.telegram.messenger.DrgIcon{i}" android:icon="@mipmap/drg_ic_{i}" android:roundIcon="@mipmap/drg_ic_{i}">'
          m = re.sub(rf"<activity-alias[^>]*{al}[^>]*>", fix_al, m, flags=re.S)
        with open(p, "w", encoding="utf-8") as file: file.write(m)
      elif f == "strings.xml":
        with open(p, "r", encoding="utf-8") as file: c = file.read()
        for k in ["AppName", "AppNameBeta", "Page1Title"]: c = re.sub(rf'(<string name="{k}">).*?(</string>)', r'\1DrgGram\2', c)
        c = re.sub(r'(<string name="Page\d+(?:Title|Message)">)(.*?)(</string>)', lambda m: m.group(1) + m.group(2).replace("Telegram", "DrgGram") + m.group(3), c, flags=re.S)
        with open(p, "w", encoding="utf-8") as file: file.write(c)
      elif f.endswith(".java") and "TMessagesProj" in p and not f.startswith("Drg"):
        with open(p, "r", encoding="utf-8") as file: jc = file.read()
        orig = jc
        jc = re.sub(r"\b(audioRecorder|audioRecord)\.read\s*\(", r"org.telegram.messenger.DrgPluginManager.readAudio(\1, ", jc)
        jc = jc.replace("LocaleController.getString(R.string.AddAccount)", "org.telegram.messenger.DrgPluginManager.formatAddAccountLabel(LocaleController.getString(R.string.AddAccount))")
        jc = jc.replace("getString(R.string.AddAccount)", "org.telegram.messenger.DrgPluginManager.formatAddAccountLabel(getString(R.string.AddAccount))")
        jc = jc.replace("LocaleController.getString(R.string.Page1Title)", '"DrgGram"')
        if jc != orig:
          with open(p, "w", encoding="utf-8") as file: file.write(jc)
    except Exception: pass

J = "TMessagesProj/src/main/java/org/telegram/"
def patch(rel, pat, rep, cnt=1):
  p = J + rel
  if os.path.exists(p):
    with open(p, "r", encoding="utf-8") as f: s = f.read()
    with open(p, "w", encoding="utf-8") as f: f.write(re.sub(pat, rep, s, count=cnt, flags=re.S))

patch("ui/IntroActivity.java", r"return\s+fragmentView\s*;", "org.telegram.messenger.DrgPluginManager.applyIntroLogoOverlay(fragmentView); return fragmentView;", 0)
patch("ui/IntroActivity.java", r"headerTextView\.setText\([^)]*\);", 'headerTextView.setText("DrgGram");', 0)
patch("ui/IntroActivity.java", r"LocaleController\.getString\(messages\[(\w+)\]\)", r'LocaleController.getString(messages[\1]).replace("Telegram", "DrgGram")', 0)
patch("ui/ActionBar/ActionBarMenuSubItem.java", r"(public\s+void\s+setTextAndIcon\s*\(\s*CharSequence\s+(\w+)\s*,\s*int\s+(\w+)[^)]*\)\s*\{)", r'\1 if (\2 != null && \2.toString().contains("/100)")) { org.telegram.messenger.DrgPluginManager.attachAccountReorderHook(this, textView); }', 0)
patch("ui/ActionBar/ActionBarPopupWindow.java", r"(public\s+void\s+showAsDropDown\s*\([^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgProxy.onPopupShown(this);", 0)
patch("ui/ActionBar/ActionBarPopupWindow.java", r"(public\s+void\s+showAtLocation\s*\([^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgProxy.onPopupShown(this);", 0)
patch("ui/ActionBar/BottomSheet.java", r"(public\s+void\s+show\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgProxy.onBottomSheetShown(this);", 0)
patch("ui/ActionBar/BaseFragment.java", r"(public\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 if (this instanceof org.telegram.ui.ProxyListActivity) { org.telegram.messenger.DrgProxy.attachToNativeProxyList(this, fragmentView); }", 0)
patch("ui/ActionBar/BaseFragment.java", r"(public\s+void\s+onTransitionAnimationEnd\s*\(\s*boolean\s+(\w+)\s*,\s*boolean\s+(\w+)\s*\)\s*\{)", r"\1 if (\2 && this instanceof org.telegram.ui.ProxyListActivity) { org.telegram.messenger.DrgProxy.attachToNativeProxyList(this, fragmentView); }", 0)
patch("ui/ProxyListActivity.java", r"(return\s+fragmentView\s*;)", r"org.telegram.messenger.DrgProxy.attachToNativeProxyList(this, fragmentView); \1", 0)
patch("ui/ProxyListActivity.java", r"(public\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgProxy.attachToNativeProxyList(this, fragmentView);", 0)
patch("ui/ProxyListActivity.java", r"((?:private|public|protected)\s+void\s+checkProxyList\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgProxy.regRecheck(this, () -> checkProxyList());", 0)
patch("messenger/ApplicationLoader.java", r"Build\.MANUFACTURER\s*\+\s*Build\.MODEL", "org.telegram.messenger.DrgPluginManager.getFakeDeviceModel(UserConfig.selectedAccount)", 0)
patch("messenger/ApplicationLoader.java", r'"SDK\s*"\s*\+\s*Build\.VERSION\.SDK_INT', "org.telegram.messenger.DrgPluginManager.getFakeSystemVersion(UserConfig.selectedAccount)", 0)
patch("messenger/BuildVars.java", r'APP_ID\s*=\s*\d+\s*;', 'APP_ID = 20904539;')
patch("messenger/BuildVars.java", r'APP_HASH\s*=\s*"[^"]*"\s*;', 'APP_HASH = "bd8349bb595e55fd6cd5e0e8bfc83faf";')
patch("messenger/BuildVars.java", r'SAFETYNET_KEY\s*=\s*"[^"]*"\s*;', 'SAFETYNET_KEY = "";')
patch("messenger/BuildVars.java", r'SUPPORTS_PASSKEYS\s*=\s*true\s*;', 'SUPPORTS_PASSKEYS = false;')
patch("messenger/BuildVars.java", r'SUPPORTS_FPNV\s*=\s*true\s*;', 'SUPPORTS_FPNV = false;')
patch("messenger/LocaleController.java", r"(public\s+static\s+String\s+formatUserStatus\s*\(\s*(?:final\s+)?int\s+(\w+)\s*,\s*(?:final\s+)?TLRPC\.User\s+(\w+)[^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.checkOnline(\2, \3);", 0)
patch("messenger/LocaleController.java", r"(public\s+static\s+String\s+getString\s*\(\s*String\s+(\w+)\s*,\s*int\s+(\w+)\s*\)\s*\{)", r'\1 if ("Page1Title".equals(\2) || "AppName".equals(\2) || "AppNameBeta".equals(\2)) return "DrgGram"; if ("AddAccount".equals(\2)) return org.telegram.messenger.DrgPluginManager.formatAddAccountLabel(null); if (\2 != null && \2.startsWith("Page")) { String v = getInstance().getStringInternal(\2, \3); return v != null ? v.replace("Telegram", "DrgGram") : v; }', 0)
patch("messenger/UserConfig.java", r"MAX_ACCOUNT_DEFAULT_COUNT\s*=\s*\d+\s*;", "MAX_ACCOUNT_DEFAULT_COUNT = 100;")
patch("messenger/UserConfig.java", r"MAX_ACCOUNT_COUNT\s*=\s*\d+\s*;", "MAX_ACCOUNT_COUNT = 100;")
patch("messenger/UserConfig.java", r"(public\s+static\s+int\s+getMaxAccountCount\s*\(\s*\)\s*\{)", r"\1 if (true) return 100;")
patch("tgnet/ConnectionsManager.java", r"(public\s+void\s+init\s*\([^)]*String\s+deviceModel\s*,\s*String\s+systemVersion[^)]*\)\s*\{)", r"\1 deviceModel = org.telegram.messenger.DrgPluginManager.getFakeDeviceModel(currentAccount); systemVersion = org.telegram.messenger.DrgPluginManager.getFakeSystemVersion(currentAccount); regId = org.telegram.messenger.DrgPluginManager.getIsolatedRegId(currentAccount, regId);", 0)
patch("tgnet/ConnectionsManager.java", r"native_init\s*\(\s*currentAccount\s*,\s*version\s*,\s*layer\s*,\s*apiId\s*,\s*deviceModel\s*,\s*systemVersion", "native_init(currentAccount, version, layer, apiId, org.telegram.messenger.DrgPluginManager.getFakeDeviceModel(currentAccount), org.telegram.messenger.DrgPluginManager.getFakeSystemVersion(currentAccount)", 0)
patch("tgnet/ConnectionsManager.java", r"(public\s+void\s+setAppPaused\s*\(\s*boolean\s+(\w+)\s*,\s*boolean\s+(\w+)\s*\)\s*\{)", r"\1 if (org.telegram.messenger.DrgPluginManager.shouldKeepAwake(currentAccount)) { \2 = false; \3 = false; }", 0)
patch("tgnet/ConnectionsManager.java", r"(private\s+void\s+sendRequestInternal\s*\(\s*TLObject\s+(\w+)[^)]*\)\s*\{)", r"\1 if (org.telegram.messenger.DrgPluginManager.shouldBlockRequest(currentAccount, \2)) return;")
patch("ui/ActionBar/ActionBarMenuItem.java", r"public\s+void\s+toggleSubMenu\s*\(", 'public boolean drgInjected = false; public android.view.View drgSubBtn = null; private void injectDrgMenuIfNeeded() { if (!drgInjected && hasSubMenu()) { drgInjected = true; try { drgSubBtn = addSubItem(999777, org.telegram.messenger.R.drawable.msg_settings, "DrgPlugins"); if (drgSubBtn != null) { drgSubBtn.setOnClickListener(v -> { org.telegram.messenger.DrgPluginManager.openDrgMenu(this, drgSubBtn, popupLayout, popupWindow); }); } } catch (Throwable ignored) {} } if (drgSubBtn != null) { org.telegram.messenger.DrgPluginManager.relocateSubItemToSwipeBack(drgSubBtn, popupLayout); } } public void toggleSubMenu(')
patch("ui/ActionBar/ActionBarMenuItem.java", r"(public\s+void\s+toggleSubMenu\s*\([^)]*\)\s*\{)", r"\1 new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> injectDrgMenuIfNeeded());", 0)
patch("ui/Cells/ChatMessageCell.java", r"(public\s+void\s+measureTime\s*\(\s*MessageObject\s+(\w+)\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onMessageObserved(currentAccount, \2);")
patch("ui/Cells/ChatMessageCell.java", r"(currentTimeString\s*=\s*[^;]+;)", r"\1 if (currentMessageObject != null && org.telegram.messenger.DrgPluginManager.isMessageDeleted(currentMessageObject.getId())) { currentTimeString = org.telegram.messenger.DrgPluginManager.prependSvgTrashSpan(currentTimeString); }", 0)
patch("ui/DialogsActivity.java", r"(public\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onDialogsResume();", 0)
patch("ui/ChatActivity.java", r"(public\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onChatResume(getDialogId(), currentAccount, getParentActivity());", 0)
patch("ui/ChatActivity.java", r">=\s*100\b", ">= (org.telegram.messenger.DrgPluginManager.isUnlimitedSelect(currentAccount) ? 100000 : 100)", 0)
patch("ui/ChatActivity.java", r">\s*100\b", "> (org.telegram.messenger.DrgPluginManager.isUnlimitedSelect(currentAccount) ? 100000 : 100)", 0)
patch("ui/ProfileActivity.java", r"(public\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.attachProfileBadge(fragmentView, currentAccount, userId);", 0)
patch("ui/LaunchActivity.java", r"((?:public|protected)\s+void\s+onResume\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onLaunchResume(this);", 0)
patch("ui/Components/ChatActivityEnterView.java", r"(public\s+boolean\s+processSendingText\s*\(\s*)(?:final\s+)?(CharSequence\s+(\w+)[^)]*\)\s*\{)", r"\1\2 \3 = org.telegram.messenger.DrgPluginManager.processOutgoingWithTiming(currentAccount, \3);")
patch("messenger/MessagesController.java", r"(public\s+void\s+deleteMessages\s*\(\s*ArrayList<Integer>\s+(\w+)\s*,\s*ArrayList<Long>\s+(\w+)\s*,\s*TLRPC\.EncryptedChat\s+(\w+)\s*,\s*long\s+(\w+)\s*,\s*int\s+(\w+)\s*,\s*boolean\s+(\w+)\s*,\s*int\s+(\w+)\s*\)\s*\{)", r"\1 if (org.telegram.messenger.DrgPluginManager.handleChunkedDelete(currentAccount, \2, \3, \4, \5, \6, \7, \8)) return;")
patch("messenger/MessagesController.java", r"(public\s+boolean\s+putUser\s*\(\s*(?:final\s+)?TLRPC\.User\s+(\w+)\s*,\s*(?:final\s+)?boolean\s+(\w+)[^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onUserPut(currentAccount, \2, \3);", 0)
patch("messenger/MessagesController.java", r"(public\s+void\s+putUsers\s*\(\s*(?:final\s+)?ArrayList<TLRPC\.User>\s+(\w+)\s*,\s*(?:final\s+)?boolean\s+(\w+)[^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onUsersPut(currentAccount, \2, \3);", 0)
patch("messenger/MessagesController.java", r"(public\s+boolean\s+processUpdateArray\s*\(\s*ArrayList<TLRPC\.Update>\s+(\w+)[^)]*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.onProcessUpdates(currentAccount, \2);")
patch("messenger/MessagesController.java", r"(private\s+void\s+updateInterfaceWithMessages\s*\(\s*long\s+(\w+)\s*,\s*ArrayList<MessageObject>\s+(\w+)[^)]*\)\s*\{)", r"\1 if (\3 != null) { for (MessageObject mo : \3) org.telegram.messenger.DrgPluginManager.onMessageObserved(currentAccount, mo); }")
patch("messenger/MessagesController.java", r"(public\s+void\s+deleteMessagesByPush\s*\([^)]*\)\s*\{)", r"\1 if (org.telegram.messenger.DrgPluginManager.isAntiDelete(currentAccount)) return;")
patch("messenger/NotificationsService.java", r"(public\s+void\s+onCreate\s*\(\s*\)\s*\{)", r"\1 org.telegram.messenger.DrgPluginManager.ensureForegroundService(this);")
# === КІНЕЦЬ ФАЙЛУ ===
