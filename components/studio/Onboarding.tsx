"use client";

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';

const steps = [
  {
    title: '✨ 3D 데이터 캔버스',
    content: '마우스 좌클릭으로 화면 좌표를 이동하고, 우클릭으로 각도를 돌려보세요. 휠을 사용해 확대/축소할 수 있습니다.',
    target: 'canvas'
  },
  {
    title: '🚀 데이터 분석 시작',
    content: '+ NEW MAPPING 버튼을 눌러 텍스트를 입력하거나 파일을 업로드하여 AI 분석을 시작하세요.',
    target: 'new-mapping'
  },
  {
    title: '📊 논리 도식화 (Diagram)',
    content: 'Diagram 토글을 켜면 하단에 데이터 간의 관계가 4열 그리드로 정렬되어 나타납니다.',
    target: 'diagram'
  },
  {
    title: '🔍 실시간 검색 및 포커스',
    content: '하단바의 검색창을 통해 특정 데이터를 찾고, 클릭하여 해당 노드로 즉시 시점을 이동할 수 있습니다.',
    target: 'search'
  }
];

interface OnboardingProps {
  onComplete: () => void;
}

const Onboarding = ({ onComplete }: OnboardingProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setIsVisible(true), 500);
    return () => clearTimeout(timer);
  }, []);

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(prev => prev + 1);
    } else {
      onComplete();
    }
  };

  return (
    <Modal
      isOpen={isVisible}
      onClose={onComplete}
      title="Studio Guide"
      size="sm"
      footer={
        <div className="flex justify-between items-center w-full">
          <div className="flex gap-1.5">
            {steps.map((_, idx) => (
              <div 
                key={idx} 
                className={`h-1 rounded-full transition-all duration-300 ${
                  idx === currentStep ? 'w-6 bg-blue-600' : 'w-2 bg-slate-200'
                }`} 
              />
            ))}
          </div>
          
          <Button 
            variant="primary"
            onClick={handleNext}
            className="px-8 py-3 bg-slate-900 hover:bg-black"
          >
            {currentStep === steps.length - 1 ? '스튜디오 시작하기' : '다음 단계'}
          </Button>
        </div>
      }
    >
      <div className="space-y-6">
        <h3 className="text-2xl font-black text-slate-900 tracking-tighter italic">
          {steps[currentStep].title}
        </h3>
        <p className="text-slate-600 text-sm leading-relaxed font-medium">
          {steps[currentStep].content}
        </p>
      </div>
    </Modal>
  );
};

export default Onboarding;
