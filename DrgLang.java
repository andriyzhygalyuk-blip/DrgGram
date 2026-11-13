package org.telegram.messenger;
import android.content.Context;
import java.util.*;

public class DrgLang {
  private static final String[] LANGS={"en","uk","ru","ar","fa","tr","de","fr","es","it","pt","pl","id","hi","zh","ja","ko","uz","kk","be","az","vi","th","nl","cs","ro","he"};
  private static final Map<String,String[]> M=new HashMap<>();

  static {
    M.put("refresh",new String[]{
      "Refresh","Оновити","Обновить","تحديث","به‌روزرسانی","Yenile","Aktualisieren","Actualiser","Actualizar","Aggiorna","Atualizar","Odśwież","Segarkan","ताज़ा करें","刷新","更新","새로고침","Yangilash","Жаңарту","Абнавіць","Yenilə","Làm mới","รีเฟรช","Vernieuwen","Obnovit","Reîmprospătare","רענן"
    });
    M.put("unavailable",new String[]{
      "Unavailable","Недоступні","Недоступные","غير متاح","در دسترس نیست","Kullanılamıyor","Nicht verfügbar","Indisponible","No disponible","Non disponibile","Indisponível","Niedostępne","Tidak tersedia","अनुपलब्ध","不可用","利用不可","사용 불가","Mavjud emas","Қолжетімсіз","Недаступныя","Əlçatmaz","Không khả dụng","ไม่พร้อมใช้งาน","Niet beschikbaar","Nedostupné","Indisponibil","לא זמין"
    });
    M.put("refreshing",new String[]{
      "Refreshing proxy list...","Оновлення списку проксі...","Обновление списка прокси...","جارٍ تحديث قائمة البروكسي...","در حال به‌روزرسانی لیست پروکسی...","Proxy listesi yenileniyor...","Proxy-Liste wird aktualisiert...","Actualisation des proxys...","Actualizando lista de proxys...","Aggiornamento lista proxy...","Atualizando lista de proxies...","Odświeżanie listy proxy...","Menyegarkan daftar proxy...","प्रॉक्सी सूची ताज़ा हो रही है...","正在刷新代理列表...","プロキシ一覧を更新中...","프록시 목록 새로고침 중...","Proksi ro'yxati yangilanmoqda...","Прокси тізімі жаңартылуда...","Абнаўленне спіса проксі...","Proksi siyahısı yenilənir...","Đang làm mới danh sách proxy...","กำลังรีเฟรชรายการพร็อกซี...","Proxylijst vernieuwen...","Obnovování seznamu proxy...","Se reîmprospătează lista proxy...","מרענן רשימת פרוקסי..."
    });
    M.put("removed_n",new String[]{
      "Removed unavailable: ","Видалено недоступних: ","Удалено недоступных: ","تم حذف غير المتاح: ","پروکسی‌های نامعتبر حذف شد: ","Kullanılamayan silindi: ","Nicht verfügbare entfernt: ","Indisponibles supprimés : ","No disponibles eliminados: ","Non disponibili rimossi: ","Indisponíveis removidos: ","Usunięto niedostępne: ","Tidak tersedia dihapus: ","अनुपलब्ध हटाए गए: ","已删除不可用代理: ","利用不可を削除: ","사용 불가 삭제됨: ","Mavjud emaslar o'chirildi: ","Қолжетімсіздер өшірілді: ","Выдалена недаступных: ","Əlçatmazlar silindi: ","Đã xóa không khả dụng: ","ลบที่ใช้งานไม่ได้แล้ว: ","Niet-beschikbaar verwijderd: ","Odstraněno nedostupných: ","Indisponibile eliminate: ","הוסרו לא זמינים: "
    });
    M.put("no_unavail",new String[]{
      "No unavailable proxies","Немає недоступних проксі","Нет недоступных прокси","لا يوجد بروكسي غير متاح","پروکسی غیرفعالی وجود ندارد","Kullanılamayan proxy yok","Keine nicht verfügbaren Proxys","Aucun proxy indisponible","No hay proxys no disponibles","Nessun proxy non disponibile","Sem proxies indisponíveis","Brak niedostępnych proxy","Tidak ada proxy tidak tersedia","कोई अनुपलब्ध प्रॉक्सी नहीं","没有不可用的代理","利用不可なプロキシはありません","사용 불가 프록시 없음","Mavjud emas proksilar yo'q","Қолжетімсіз прокси жоқ","Няма недаступных проксі","Əlçatmaz proksi yoxdur","Không có proxy nào lỗi","ไม่มีพร็อกซีที่ใช้งานไม่ได้","Geen onbeschikbare proxy's","Žádné nedostupné proxy","Nu există proxy-uri indisponibile","אין שרתי פרוקסי לא זמינים"
    });
    M.put("type",new String[]{
      "Type","Тип","Тип","النوع","نوع","Tür","Typ","Type","Tipo","Tipo","Tipo","Typ","Tipe","प्रकार","类型","種類","유형","Turi","Түрі","Тып","Növ","Loại","ประเภท","Type","Typ","Tip","סוג"
    });
    M.put("mtproto",new String[]{
      "MTProto Proxy","Проксі MTProto","Прокси MTProto","بروكسي MTProto","پروکسی MTProto","MTProto Proxy","MTProto-Proxy","Proxy MTProto","Proxy MTProto","Proxy MTProto","Proxy MTProto","Proxy MTProto","Proxy MTProto","MTProto प्रॉक्सी","MTProto 代理","MTProto プロキシ","MTProto 프록시","MTProto Proksi","MTProto Прокси","Проксі MTProto","MTProto Proksi","Proxy MTProto","พร็อกซี MTProto","MTProto-proxy","MTProto Proxy","Proxy MTProto","פרוקסי MTProto"
    });
    M.put("country",new String[]{
      "Country","Країна","Страна","الدولة","کشور","Ülke","Land","Pays","País","Paese","País","Kraj","Negara","देश","国家","国","국가","Mamlakat","Ел","Краіна","Ölkə","Quốc gia","ประเทศ","Land","Země","Țară","מדינה"
    });
    M.put("bg_sel_err",new String[]{
      "This account cannot be disabled while it is selected.","Акаунт неможливо вимкнути, поки він вибраний.","Аккаунт невозможно отключить, пока он выбран.","لا يمكن إيقاف تشغيل الحساب طالما أنه محدد حالياً.","تا زمانی که این حساب انتخاب شده است، امکان غیرفعال کردن آن وجود ندارد.","Seçili olduğu sürece bu hesap devre dışı bırakılamaz.","Das Konto kann nicht deaktiviert werden, solange es ausgewählt ist.","Impossible de désactiver le compte tant qu'il est sélectionné.","No se puede desactivar la cuenta mientras esté seleccionada.","Impossibile disattivare l'account mentre è selezionato.","Não é possível desativar a conta enquanto ela estiver selecionada.","Nie można wyłączyć konta, dopóki jest wybrane.","Akun tidak dapat dinonaktifkan selama masih dipilih.","चयनित रहते समय इस खाते को अक्षम नहीं किया जा सकता।","当前选中的账号无法被关闭。","選択中のアカウントは無効にできません。","현재 선택된 계정은 비활성화할 수 없습니다.","Tanlangan vaqtda ushbu akkauntni o'chirib bo'lmaydi.","Таңдалып тұрған кезде бұл аккаунтты өшіру мүмкін емес.","Акаунт немагчыма адключыць, пакуль ён выбраны.","Seçili olduğu müddətdə bu hesabı söndürmək olmaz.","Không thể tắt tài khoản khi tài khoản này đang được chọn.","ไม่สามารถปิดบัญชีนี้ได้ในขณะที่กำลังเลือกใช้งานอยู่","Het account kan niet worden uitgeschakeld zolang het is geselecteerd.","Účet nelze vypnout, dokud je vybrán.","Contul nu poate fi dezactivat cât timp este selectat.","לא ניתן להשבית את החשבון בזמן שהוא נבחר."
    });
    M.put("bg_limit_title",new String[]{
      "Anti-Fraud Limit","Анти-фрод обмеження","Анти-фрод ограничение","حد مكافحة الاحتيال","محدودیت ضد تقلب","Anti-Fraud Sınırı","Anti-Fraud-Limit","Limite anti-fraude","Límite antifraude","Limite anti-frode","Limite antifraude","Limit antyfraudowy","Batas Anti-Penipuan","एंटी-फ्रॉड सीमा","防风控限制","アンチフラウド制限","안티 프로드 제한","Anti-frod cheklovi","Анти-фрод шектеуі","Анты-фрод абмежаванне","Anti-frod limiti","Giới hạn chống gian lận","ขีดจำกัดป้องกันแอนตี้ฟรอด","Anti-fraudelimiet","Limit anti-fraud","Limită anti-fraudă","מגבלת אנטי-הונאה"
    });
    M.put("bg_limit_msg",new String[]{
      "To prevent triggering Telegram's anti-fraud system, you can select at most 10 accounts for background operation.","Для запобігання спрацювання анти-фрод системи Telegram можна вибрати тільки 10 акаунтів для роботи у фоновому режимі.","Для предотвращения срабатывания анти-фрод системы Telegram можно выбрать только 10 акаунтів для работы в фоновом режиме.","لمنع تفعيل نظام مكافحة الاحتيال في تيليجرام، يمكنك اختيار 10 حسابات فقط للعمل في الخلفية.","برای جلوگیری از فعال شدن سیستم ضد تقلب تلگرام، حداکثر ۱۰ حساب برای کار در پس‌زمینه قابل انتخاب است.","Telegram anti-fraud sisteminin tetiklenmesini önlemek için arka planda çalışacak en fazla 10 hesap seçebilirsiniz.","Um das Anti-Fraud-System von Telegram nicht auszulösen, können maximal 10 Konten für den Hintergrundbetrieb ausgewählt werden.","Pour éviter de déclencher le système anti-fraude de Telegram, vous pouvez sélectionner au maximum 10 comptes en arrière-plan.","Para evitar activar el sistema antifraude de Telegram, solo puedes seleccionar hasta 10 cuentas en segundo plano.","Per evitare il sistema anti-frode di Telegram, puoi selezionare al massimo 10 account attivi in background.","Para evitar acionar o sistema antifraude do Telegram, você pode selecionar no máximo 10 contas em segundo plano.","Aby uniknąć blokady antyfraudowej Telegrama, możesz wybrać maksymalnie 10 kont działających w tle.","Untuk mencegah sistem anti-penipuan Telegram, maksimal 10 akun dapat dipilih untuk berjalan di latar belakang.","टेलीग्राम के एंटी-फ्रॉड सिस्टम से बचने के लिए, आप बैकग्राउंड में अधिकतम 10 खाते ही चुन सकते हैं।","为防止触发 Telegram 风控系统，后台运行最多只能选择 10 个账号。","Telegramの不正検知を防ぐため、バックグラウンド動作は最大10アカウントまで選択できます。","Telegram 안티 프로드 시스템 작동을 방지하기 위해 백그라운드 작업은 최대 10개 계정만 선택할 수 있습니다.","Telegram anti-frod tizimi ishga tushishini oldini olish uchun fonda ishlashga ko'pi bilan 10 ta akkaunt tanlash mumkin.","Telegram анти-фрод жүйесі іске қосылмауы үшін фонда жұмыс істеуге ең көбі 10 аккаунт таңдауға болады.","Каб прадухіліць спрацоўванне анты-фрод сістэмы Telegram, можна выбраць толькі 10 акаунтаў для працы ў фоне.","Telegram anti-frod sisteminin işə düşməməsi üçün arxa fonda işləmək üçün maksimum 10 hesab seçə bilərsiniz.","Để tránh kích hoạt hệ thống chống gian lận của Telegram, chỉ có thể chọn tối đa 10 tài khoản chạy nền.","เพื่อป้องกันระบบแอนตี้ฟรอดของ Telegram สามารถเลือกบัญชีทำงานเบื้องหลังได้สูงสุด 10 บัญชี","Om het anti-fraudesysteem van Telegram te voorkomen, kun je maximaal 10 accounts voor achtergrondgebruik selecteren.","Aby nedošlo ke spuštění anti-fraud systému Telegramu, lze pro práci na pozadí vybrat maximálně 10 účtů.","Pentru a preveni sistemul anti-fraudă Telegram, poți selecta maximum 10 conturi pentru rulare în fundal.","כדי למנוע הפעלת מערכת האנטי-הונאה של טלגרם, ניתן לבחור עד 10 חשבונות בלבד לפעילות ברקע."
    });
  }

  public static String lang(){
    try{
      String l=LocaleController.getInstance().getCurrentLocaleInfo().getLangCode();
      if(l==null||l.isEmpty())l=Locale.getDefault().getLanguage();
      l=l.toLowerCase(Locale.US);
      int cut=l.indexOf('-');
      if(cut<0)cut=l.indexOf('_');
      return cut>0?l.substring(0,cut):l;
    }catch(Throwable t){return "en";}
  }

  private static int langIdx(){
    String l=lang();
    if("ua".equals(l))return 1;
    for(int i=0;i<LANGS.length;i++)if(LANGS[i].equals(l))return i;
    return 0;
  }

  public static String get(String key){
    String[] arr=M.get(key);
    if(arr==null||arr.length==0)return key;
    int idx=langIdx();
    return idx<arr.length?arr[idx]:arr[0];
  }

  public static String tgRaw(String tgKey){
    if(tgKey==null||tgKey.isEmpty())return "";
    try{
      Context c=ApplicationLoader.applicationContext;
      String[] pkgs={"org.telegram.messenger","org.telegram.messenger.web",c.getPackageName()};
      for(String pkg:pkgs){
        int id=c.getResources().getIdentifier(tgKey,"string",pkg);
        if(id!=0){
          String s=LocaleController.getString(id);
          if(s!=null&&!s.isEmpty())return s;
        }
      }
    }catch(Throwable ignored){}
    return "";
  }

  public static String tg(String tgKey,String customKey){
    String s=tgRaw(tgKey);
    if(!s.isEmpty())return s;
    return get(customKey);
  }

  public static boolean isDialogsMenuLabel(String raw){
    if(raw==null||raw.isEmpty())return false;
    String l=raw.toLowerCase(Locale.ROOT).trim();
    String[] keys={"NewGroup","NewSecretChat","NewChannel","Contacts","Calls","SavedMessages","ArchivedChats","Settings","Wallet","InviteFriends","ReadAllChats"};
    for(String k:keys){
      String v=tgRaw(k);
      if(!v.isEmpty()&&l.equals(v.toLowerCase(Locale.ROOT).trim()))return true;
    }
    return l.contains("група")||l.contains("группа")||l.contains("group")||l.contains("архів")||l.contains("архив")||l.contains("archive")||l.contains("збережене")||l.contains("избранное")||l.contains("saved")||l.contains("гаманець")||l.contains("кошелек")||l.contains("wallet");
  }

  public static boolean isExcludedMenuLabel(String raw){
    if(raw==null||raw.isEmpty())return false;
    String l=raw.toLowerCase(Locale.ROOT).trim();
    if(l.contains("drgplugins")||l.contains("пошук")||l.contains("поиск")||l.contains("search"))return true;
    String sr=tgRaw("Search");
    return !sr.isEmpty()&&l.equals(sr.toLowerCase(Locale.ROOT).trim());
  }
}
// === КІНЕЦЬ ФАЙЛУ ===
