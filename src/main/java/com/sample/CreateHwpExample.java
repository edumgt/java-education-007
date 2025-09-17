package com.sample;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import kr.dogfoot.hwplib.writer.HWPWriter;

public class CreateHwpExample {
    public static void main(String[] args) {
        try {
            // 1. 빈 HWP 파일 객체 생성
            HWPFile hwpFile = new HWPFile();

            // 2. Section 추가
            Section section = hwpFile.getBodyText().addNewSection();

            // 3. Paragraph 추가
            Paragraph para = section.addNewParagraph();

            // 4. ParaText 생성 후 문자열 입력
            ParaText paraText = new ParaText();
            paraText.addString("안녕하세요, hwplib 1.0.4로 생성한 문서입니다!");

            // 5. 문단에 텍스트 설정
            para.setText(paraText);

            // 6. 저장
            String savePath = "java_hwplib_104_example.hwp";
            HWPWriter.toFile(hwpFile, savePath);

            System.out.println("✅ HWP 문서 생성 완료: " + savePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
