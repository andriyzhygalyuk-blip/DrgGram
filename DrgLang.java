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

    M.put("pl_ghost",new String[]{
      "Ghost mode","Режим привида","Режим призрака","وضع الشبح","حالت روح","Hayalet modu","Geistermodus","Mode fantôme","Modo fantasma","Modalità fantasma","Modo fantasma","Tryb ducha","Mode hantu","घोस्ट मोड","幽灵模式","ゴーストモード","고스트 모드","Arvoh rejimi","Елес режимі","Рэжым здані","Kabus rejimi","Chế độ ẩn danh","โหมดผี","Spookmodus","Režim ducha","Mod fantomă","מצב רוח רפאים"
    });
    M.put("pl_fwd",new String[]{
      "Forward chat","Переслати чат","Переслать чат","إعادة توجيه الدردشة","بازارسال چت","Sohbeti ilet","Chat weiterleiten","Transférer le chat","Reenviar chat","Inoltra chat","Encaminhar chat","Przekaż czat","Teruskan obrolan","चैट फ़ॉरवर्ड करें","转发聊天","チャットを転送","채팅 전달","Chatni uzatish","Чатты жіберу","Пераслаць чат","Söhbəti yönləndir","Chuyển tiếp đoạn chat","ส่งต่อแชท","Chat doorsturen","Přeposlat chat","Redirecționează chatul","העבר צ'אט"
    });
    M.put("pl_radar",new String[]{
      "Online radar","Онлайн радар","Онлайн радар","رادار الاتصال","رادار آنلاین","Çevrimiçi radarı","Online-Radar","Radar en ligne","Radar en línea","Radar online","Radar online","Radar online","Radar online","ऑनलाइन रडार","在线雷达","オンラインレーダー","온라인 레이더","Onlayn radar","Онлайн радар","Анлайн радар","Onlayn radar","Radar trực tuyến","เรดาร์ออนไลน์","Online radar","Online radar","Radar online","רדאר אונליין"
    });
    M.put("pl_antidel",new String[]{
      "Anti-delete","Анти-видалення","Анти-удаление","منع الحذف","ضد حذف","Silme koruması","Löschschutz","Anti-suppression","Anti-eliminación","Anti-eliminazione","Anti-exclusão","Ochrona przed usuwaniem","Anti-hapus","एंटी-डिलीट","防撤回","削除防止","삭제 방지","O'chirishdan himoya","Жоюдан қорғау","Анты-выдаленне","Silinməyə qarşı","Chống xóa tin nhắn","ป้องกันการลบ","Anti-verwijderen","Ochrana proti smazání","Anti-ștergere","מניעת מחיקה"
    });
    M.put("pl_unlim",new String[]{
      "Unlimited select","Безліміт вибір","Безлимит выбор","تحديد غير محدود","انتخاب نامحدود","Sınırsız seçim","Unbegrenzte Auswahl","Sélection illimitée","Selección ilimitada","Selezione illimitata","Seleção ilimitada","Wybór bez limitu","Pilih tanpa batas","असीमित चयन","无限选择","無制限選択","무제한 선택","Cheksiz tanlash","Шексіз таңдау","Безліміт выбар","Limitsiz seçim","Chọn không giới hạn","เลือกได้ไม่จำกัด","Onbeperkt selecteren","Neomezený výběr","Selecție nelimitată","בחירה ללא הגבלה"
    });
    M.put("pl_voice",new String[]{
      "Voice changer","Зміна голосу","Изменение голоса","مغير الصوت","تغییر صدا","Ses değiştirici","Stimmenverzerrer","Modificateur de voix","Cambiador de voz","Cambia voce","Modificador de voz","Zmiana głosu","Pengubah suara","आवाज़ बदलें","变声器","ボイスチェンジャー","음성 변조","Ovozni o'zgartirish","Дауысты өзгерту","Змена голасу","Səs dəyişdirici","Đổi giọng nói","เปลี่ยนเสียง","Stemvervormer","Měnič hlasu","Schimbător de voce","משנה קול"
    });
    M.put("pl_vtime",new String[]{
      "Voice timing","Таймінг голосового","Тайминг голосового","توقيت الرسالة الصوتية","زمان‌بندی ویس","Sesli mesaj zamanı","Sprachnachricht-Timing","Minutage vocal","Tiempo del audio","Timing vocale","Tempo do áudio","Czas głosówki","Waktu pesan suara","वॉइस टाइमिंग","语音时间标记","ボイス時刻","음성 시간 표시","Ovozli xabar vaqti","Дауыс уақыты","Таймінг галасавога","Səsli mesaj vaxtı","Thời gian thoại","เวลาข้อความเสียง","Spraakbericht-timing","Časování hlasovky","Cronometru vocal","תזמון הודעה קולית"
    });
    M.put("pl_mutepm",new String[]{
      "Mute PM","Мут в ЛС","Мут в ЛС","كتم الخاص","سایلنت پی‌وی","Özel sohbeti sustur","Privat-Chat stummschalten","Muet en MP","Silenciar MP","Silenzia PM","Silenciar PV","Wycisz PW","Bisukan PM","पीएम म्यूट","私聊静音","DMをミュート","개인 채팅 음소거","Shaxsiy chatni o'chirish","Жеке чатты өшіру","Мут у ПП","Şəxsi söhbəti susdur","Tắt tiếng tin nhắn riêng","ปิดเสียงแชทส่วนตัว","Privéchat dempen","Ztlumit SZ","Dezactivează sunet PM","השתק צ'אט פרטי"
    });
    M.put("pl_delown",new String[]{
      "Delete own messages","Видалити свої СМС","Удалить свои СМС","حذف رسائلي","حذف پیام‌های خود","Kendi mesajlarımı sil","Eigene Nachrichten löschen","Supprimer mes messages","Eliminar mis mensajes","Elimina i miei messaggi","Apagar minhas mensagens","Usuń własne wiadomości","Hapus pesan sendiri","अपने संदेश हटाएं","删除自己的消息","自分のメッセージを削除","내 메시지 삭제","O'z xabarlarini o'chirish","Өз хабарларын жою","Выдаліць свае СМС","Öz mesajlarını sil","Xóa tin nhắn của tôi","ลบข้อความของตัวเอง","Eigen berichten verwijderen","Smazat vlastní zprávy","Șterge mesajele proprii","מחק את ההודעות שלי"
    });
    M.put("pl_log",new String[]{
      "Log","Журнал","Журнал","السجل","گزارش","Günlük","Protokoll","Journal","Registro","Registro","Registro","Dziennik","Log","लॉग","日志","ログ","로그","Jurnal","Журнал","Журнал","Jurnal","Nhật ký","บันทึก","Logboek","Protokol","Jurnal","יומן"
    });

    M.put("gh_msg",new String[]{
      "Don't read messages","Не читати повідомлення","Не читать сообщения","عدم قراءة الرسائل","عدم خواندن پیام‌ها","Mesajları okuma","Nachrichten nicht lesen","Ne pas lire les messages","No leer mensajes","Non leggere i messaggi","Não ler mensagens","Nie czytaj wiadomości","Jangan baca pesan","संदेश न पढ़ें","不读消息","メッセージを既読にしない","메시지 읽음 표시 안 함","Xabarlarni o'qimaslik","Хабарларды оқымау","Не чытаць паведамленні","Mesajları oxuma","Không đọc tin nhắn","ไม่อ่านข้อความ","Berichten niet lezen","Nečíst zprávy","Nu citi mesajele","אל תקרא הודעות"
    });
    M.put("gh_stories",new String[]{
      "Don't read stories","Не читати історії","Не читать истории","عدم مشاهدة القصص","عدم مشاهده استوری‌ها","Hikayeleri görme","Stories nicht ansehen","Ne pas voir les stories","No ver historias","Non vedere le storie","Não ver stories","Nie oglądaj relacji","Jangan lihat cerita","स्टोरी न देखें","不看动态","ストーリーを既読にしない","스토리 읽음 표시 안 함","Storislarni ko'rmaslik","Стористерді көрмеу","Не глядзець гісторыі","Hekayələrə baxma","Không xem tin","ไม่ดูสตอรี่","Verhalen niet bekijken","Nečíst příběhy","Nu vedea poveștile","אל תצפה בסטוריז"
    });
    M.put("gh_online",new String[]{
      "Don't send online","Не надсилати «в мережі»","Не отправлять «в сети»","عدم إرسال حالة متصل","عدم ارسال وضعیت آنلاین","Çevrimiçi gönderme","Online-Status verbergen","Masquer le statut en ligne","No enviar «en línea»","Non inviare «online»","Não enviar «online»","Nie wysyłaj «online»","Jangan kirim status online","ऑनलाइन स्थिति न भेजें","不发送在线状态","オンライン状態を送信しない","온라인 상태 전송 안 함","Onlayn holatini yubormaslik","Онлайн күйін жібермеу","Не дасылаць «у сетцы»","Onlayn statusu göndərmə","Không gửi trạng thái trực tuyến","ไม่ส่งสถานะออนไลน์","Online-status verbergen","Neposílat «online»","Nu trimite «online»","אל תשלח סטטוס מחובר"
    });
    M.put("gh_typing",new String[]{
      "Don't send typing","Не надсилати «друкую»","Не отправлять «печатаю»","عدم إرسال جاري الكتابة","عدم ارسال در حال تایپ","Yazıyor gönderme","«Schreibt» verbergen","Masquer «écrit»","No enviar «escribiendo»","Non inviare «sta scrivendo»","Não enviar «digitando»","Nie wysyłaj «pisze»","Jangan kirim status mengetik","टाइपिंग स्थिति न भेजें","不发送正在输入","入力中を送信しない","입력 중 전송 안 함","Yozmoqda holatini yubormaslik","Теру күйін жібермеу","Не дасылаць «друкуе»","Yazır statusu göndərmə","Không gửi đang nhập","ไม่ส่งสถานะกำลังพิมพ์","Typstatus verbergen","Neposílat «píše»","Nu trimite «scrie»","אל תשלח מקליד"
    });
    M.put("gh_offline",new String[]{
      "Auto offline","Автоматичний «офлайн»","Автоматический «офлайн»","غير متصل تلقائياً","آفلاین خودکار","Otomatik çevrimdışı","Auto-Offline","Hors ligne auto","Desconexión automática","Offline automatico","Offline automático","Auto offline","Offline otomatis","ऑटो ऑफ़लाइन","自动离线","自動オフライン","자동 오프라인","Avto oflayn","Авто офлайн","Аўтаматычны «афлайн»","Avto oflayn","Tự động ngoại tuyến","ออฟไลน์อัตโนมัติ","Automatisch offline","Automaticky offline","Offline automat","אופליין אוטומטי"
    });
    M.put("gh_interact",new String[]{
      "Read on interaction","Читати при взаємодіях","Читать при взаимодействиях","القراءة عند التفاعل","خواندن هنگام تعامل","Etkileşimde oku","Bei Interaktion lesen","Lire à l'interaction","Leer al interactuar","Leggi all'interazione","Ler ao interagir","Czytaj przy interakcji","Baca saat berinteraksi","इंटरैक्शन पर पढ़ें","互动时已读","操作時に既読にする","상호작용 시 읽음 처리","Aloqada o'qish","Әрекетте оқу","Чытаць пры ўзаемадзеянні","Qarşılıqlı əlaqədə oxu","Đọc khi tương tác","อ่านเมื่อมีการโต้ตอบ","Lezen bij interactie","Přečíst při interakci","Citește la interacțiune","קרא בעת אינטראקציה"
    });
    M.put("radar_empty",new String[]{
      "No people found","Немає людей у діалогах","Нет людей в диалогах","لم يتم العثور على أشخاص","هیچ شخصی یافت نشد","Kişi bulunamadı","Keine Personen gefunden","Aucune personne trouvée","No se encontraron personas","Nessuna persona trovata","Nenhuma pessoa encontrada","Nie znaleziono osób","Tidak ada orang ditemukan","कोई व्यक्ति नहीं मिला","未找到联系人","ユーザーが見つかりません","사용자를 찾을 수 없습니다","Odamlar topilmadi","Адамдар табылмады","Няма людзей у дыялогах","İnsan tapılmadı","Không tìm thấy người nào","ไม่พบบุคคล","Geen personen gevonden","Nenalezeni žádní lidé","Nu s-au găsit persoane","לא נמצאו אנשים"
    });
    M.put("mute_pm_only",new String[]{
      "Available in private chats only","Доступно лише в особистих чатах","Доступно только в личных чатах","متاح في المحادثات الخاصة فقط","فقط در چت‌های خصوصی در دسترس است","Yalnızca özel sohbetlerde kullanılabilir","Nur in privaten Chats verfügbar","Disponible uniquement en privé","Solo disponible en chats privados","Disponibile solo nelle chat private","Disponível apenas em chats privados","Dostępne tylko w czatach prywatnych","Hanya tersedia di obrolan pribadi","केवल निजी चैट में उपलब्ध","仅在私聊中可用","個人チャットでのみ利用可能","개인 채팅에서만 사용 가능","Faqat shaxsiy chatlarda mavjud","Тек жеке чаттарда қолжетімді","Даступна толькі ў асабістых чатах","Yalnız şəxsi söhbətlərdə mövcuddur","Chỉ khả dụng trong trò chuyện riêng tư","ใช้ได้เฉพาะในแชทส่วนตัวเท่านั้น","Alleen beschikbaar in privéchats","Dostupné pouze v soukromých chatech","Disponibil doar în chaturi private","זמין בצ'אטים פרטיים בלבד"
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
