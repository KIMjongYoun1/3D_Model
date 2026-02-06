"use client";

import React, { useRef, Suspense, useState, useEffect, useMemo } from "react";
import { Canvas, useFrame, useThree } from "@react-three/fiber";
import { OrbitControls, Grid, Text, Float, Line, Billboard, MeshWobbleMaterial } from "@react-three/drei";
import * as THREE from "three";

// 1. 선택된 노드 마커
function SelectionMarker({ color, isTop }: any) {
  const markerRef = useRef<THREE.Group>(null);
  useFrame((state) => {
    if (markerRef.current) {
      markerRef.current.rotation.y += 0.05;
      const s = (isTop ? 1.8 : 1.2) + Math.sin(state.clock.getElapsedTime() * 8) * 0.2;
      markerRef.current.scale.set(s, s, s);
    }
  });
  return (
    <group ref={markerRef}>
      <mesh rotation={[Math.PI / 2, 0, 0]}>
        <torusGeometry args={[1.5, 0.08, 16, 100]} />
        <meshBasicMaterial color={color} transparent opacity={0.9} />
      </mesh>
    </group>
  );
}

// 2. 플로팅 정보창 (Floating Info Window) - 컨텍스트 위치 및 GPU 가속 최적화
export function DraggableWindow({ node, onClose, zIndex, onFocus, isTop }: any) {
  const windowRef = useRef<HTMLDivElement>(null);
  
  // 전달받은 초기 위치가 있으면 사용 (중앙 보정), 없으면 랜덤 배치
  const initialX = node.initialX ? node.initialX - 160 : 100 + Math.random() * 100;
  const initialY = node.initialY ? node.initialY - 100 : 150 + Math.random() * 100;
  
  const [pos, setPos] = useState({ x: initialX, y: initialY });
  const isDragging = useRef(false);
  const dragOffset = useRef({ x: 0, y: 0 });

  const handleMouseDown = (e: React.MouseEvent) => {
    isDragging.current = true;
    onFocus(node.id);
    dragOffset.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    if (windowRef.current) windowRef.current.style.transition = 'none';
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging.current || !windowRef.current) return;
      const newX = e.clientX - dragOffset.current.x;
      const newY = e.clientY - dragOffset.current.y;
      windowRef.current.style.transform = `translate3d(${newX}px, ${newY}px, 0)`;
      pos.x = newX;
      pos.y = newY;
    };

    const handleMouseUp = () => {
      if (isDragging.current) {
        isDragging.current = false;
        setPos({ ...pos });
        if (windowRef.current) windowRef.current.style.transition = 'all 0.4s cubic-bezier(0.16, 1, 0.3, 1)';
      }
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [pos]);

  return (
    <div 
      ref={windowRef}
      onMouseDown={() => onFocus(node.id)}
      style={{ 
        transform: `translate3d(${pos.x}px, ${pos.y}px, 0)`,
        zIndex 
      }}
      className={`absolute top-0 left-0 w-[400px] bg-white/90 backdrop-blur-3xl border-2 ${isTop ? 'border-blue-500 shadow-[0_30px_80px_rgba(37,99,235,0.2)]' : 'border-slate-200 shadow-2xl'} rounded-[2.5rem] select-none overflow-hidden pointer-events-auto transition-all duration-400 transform-gpu`}
    >
      <div onMouseDown={handleMouseDown} className={`p-6 flex justify-between items-center cursor-move border-b ${isTop ? 'bg-blue-50/50 border-blue-100' : 'bg-slate-50/50 border-slate-100'}`}>
        <div className="flex items-center gap-3">
          <div className={`w-2.5 h-2.5 rounded-full ${isTop ? 'bg-blue-600' : 'bg-slate-400'} animate-pulse`} />
          <span className="text-[11px] font-black tracking-[0.15em] text-slate-800 uppercase truncate w-64">{node.label}</span>
        </div>
        <button onClick={(e) => { e.stopPropagation(); onClose(node.id); }} className="text-slate-400 hover:text-slate-900 transition-colors p-1">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
        </button>
      </div>
      
      <div className="p-8 space-y-6 select-text overflow-y-auto max-h-[600px] custom-scrollbar">
        {/* 1. Raw Data 섹션 */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 px-1">
            <div className="w-1 h-3 bg-blue-500 rounded-full" />
            <span className="text-[10px] font-black text-blue-600 uppercase tracking-widest">Raw_Data</span>
          </div>
          <div className="bg-slate-50/80 p-5 rounded-2xl border border-slate-100 shadow-inner max-h-40 overflow-y-auto custom-scrollbar">
            <p className="text-[12px] font-medium text-slate-700 leading-relaxed break-words">
              {typeof node.value === 'string' && node.value.includes('● 데이터:') 
                ? node.value.split('● 데이터:')[1].split('● 해석:')[0].trim()
                : (typeof node.value === 'string' ? node.value : JSON.stringify(node.value, null, 2))}
            </p>
          </div>
        </div>

        {/* 2. AI 해석/요약 섹션 */}
        {(typeof node.value === 'string' && node.value.includes('● 해석:')) && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 px-1">
              <div className="w-1 h-3 bg-emerald-500 rounded-full" />
              <span className="text-[10px] font-black text-emerald-600 uppercase tracking-widest">AI_Interpretation</span>
            </div>
            <div className="bg-emerald-50/30 p-5 rounded-2xl border border-emerald-100 shadow-inner">
              <p className="text-[12px] font-bold text-slate-800 leading-relaxed break-words">
                {node.value.split('● 해석:')[1].trim()}
              </p>
            </div>
          </div>
        )}

        {/* 3. 참고 자료 및 근거 섹션 */}
        {node.references && node.references.length > 0 && (
          <div className="space-y-3">
            <div className="flex items-center gap-2 px-1">
              <div className="w-1 h-3 bg-slate-400 rounded-full" />
              <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">References & Evidence</span>
            </div>
            <div className="space-y-2">
              {node.references.map((ref: any, idx: number) => (
                <div key={idx} className="bg-slate-100/50 p-4 rounded-2xl border border-slate-200 group/ref transition-all hover:bg-white hover:shadow-md">
                  <div className="flex justify-between items-start mb-1">
                    <span className="text-[10px] font-black text-slate-700 truncate w-48">{ref.title}</span>
                    <a href={ref.url} target="_blank" className="text-blue-600 hover:text-blue-800 transition-colors">
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" /></svg>
                    </a>
                  </div>
                  {ref.snippet && <p className="text-[9px] text-slate-500 leading-normal italic line-clamp-2">"{ref.snippet}"</p>}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// 3. 3D 노드 (화이트 배경 최적화 및 울트라 볼드 텍스트)
function Node({ node, isHovered, isSelected, isTop, onHover, onClick, centerOffset }: any) {
  const { camera, size } = useThree();
  const color = isTop ? "#1e40af" : (isSelected ? "#2563eb" : (isHovered ? "#0f172a" : "#94a3b8"));
  const scale = isHovered ? 2.8 : (isTop ? 2.2 : 1.8);

  const handleNodeClick = (e: any) => {
    e.stopPropagation();
    const worldPos = new THREE.Vector3(
      node.pos[0] + (centerOffset?.[0] || 0),
      node.pos[1] + (centerOffset?.[1] || 0),
      node.pos[2] + (centerOffset?.[2] || 0)
    );
    worldPos.project(camera);
    const x = (worldPos.x + 1) * size.width / 2;
    const y = -(worldPos.y - 1) * size.height / 2;
    onClick(node, { x, y });
  };

  return (
    <group position={node.pos} onPointerOver={() => onHover(node.id)} onPointerOut={() => onHover(null)} onClick={handleNodeClick}>
      <mesh visible={false}><sphereGeometry args={[2.0, 8, 8]} /></mesh>
      <Float speed={isTop ? 8 : 2} rotationIntensity={0.5} floatIntensity={0.5}>
        <mesh scale={scale}>
          <sphereGeometry args={[0.6, 32, 32]} />
          <meshStandardMaterial color={color} emissive={color} emissiveIntensity={isTop ? 2 : 0.5} />
        </mesh>
        {(isSelected || isTop) && <SelectionMarker color={isTop ? "#1e40af" : "#2563eb"} isTop={isTop} />}
      </Float>
      <Billboard position={[0, 3.5, 0]}>
        <Text 
          fontSize={isHovered || isTop ? 1.4 : 1.0} 
          color={isTop ? "#1e3a8a" : (isSelected ? "#2563eb" : (isHovered ? "#0f172a" : "#64748b"))} 
          fontWeight={900}
          anchorX="center"
          anchorY="middle"
          outlineWidth={0.2}
          outlineColor="#ffffff"
        >
          {isTop ? `✓ ${node.label}` : node.label}
        </Text>
      </Billboard>
    </group>
  );
}

// 4. 정산 데이터를 위한 '바(Bar)' 컴포넌트
function SettlementBar({ node, isHovered, isSelected, isTop, onHover, onClick, centerOffset }: any) {
  const { camera, size } = useThree();
  const height = Math.max(2, node.pos[1]);
  const color = isTop ? "#1e40af" : (isSelected ? "#10b981" : "#38bdf8");
  
  const handleNodeClick = (e: any) => {
    e.stopPropagation();
    const worldPos = new THREE.Vector3(
      node.pos[0] + (centerOffset?.[0] || 0),
      (height / 2) + (centerOffset?.[1] || 0),
      node.pos[2] + (centerOffset?.[2] || 0)
    );
    worldPos.project(camera);
    const x = (worldPos.x + 1) * size.width / 2;
    const y = -(worldPos.y - 1) * size.height / 2;
    onClick(node, { x, y });
  };

  return (
    <group position={[node.pos[0], 0, node.pos[2]]} onPointerOver={() => onHover(node.id)} onPointerOut={() => onHover(null)} onClick={handleNodeClick}>
      <mesh position={[0, height / 2, 0]}>
        <boxGeometry args={[1.5, height, 1.5]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={isHovered || isTop ? 2 : 0.5} transparent opacity={0.9} />
      </mesh>
      {(isSelected || isTop) && <SelectionMarker color={color} isTop={isTop} />}
      <Billboard position={[0, height + 1.5, 0]}>
        <Text fontSize={1.0} color={color} fontWeight={900} outlineWidth={0.1} outlineColor="white">
          {node.label}
        </Text>
      </Billboard>
    </group>
  );
}

// 5. 비구조화 데이터를 위한 '모놀리스' 컴포넌트
function RawDataMonolith({ data, onClick }: any) {
  const meshRef = useRef<THREE.Mesh>(null);
  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.005;
      meshRef.current.position.y = Math.sin(state.clock.getElapsedTime()) * 0.5;
    }
  });
  return (
    <group onClick={() => onClick({ id: 'raw', label: 'RAW_DATA', value: data.content, type: 'text', pos: [0,0,0] })}>
      <Float speed={2} rotationIntensity={0.5} floatIntensity={0.5}>
        <mesh ref={meshRef}>
          <boxGeometry args={[4, 8, 1]} />
          <MeshWobbleMaterial color="#3b82f6" factor={0.2} speed={1} emissive="#3b82f6" emissiveIntensity={0.2} />
        </mesh>
      </Float>
      <Billboard position={[0, 6, 0]}>
        <Text fontSize={1.2} color="#1e293b" fontWeight={900} outlineWidth={0.1} outlineColor="white">RAW_DATA_MONOLITH</Text>
        <Text position={[0, -1.2, 0]} fontSize={0.5} color="#64748b" fontWeight={700}>UNSTRUCTURED BLOB detected</Text>
      </Billboard>
    </group>
  );
}

// 6. 지능형 카메라 및 시점 매니저 (일회성 이동 후 제어권 반환)
function CameraController({ topNodeId, data, centerOffset, autoFocus }: any) {
  const { camera, controls } = useThree() as any;
  const lastTargetId = useRef<string | null>(null);

  useFrame(() => {
    if (autoFocus && topNodeId && data?.nodes && controls && lastTargetId.current !== topNodeId) {
      const node = data.nodes.find((n: any) => n.id === topNodeId);
      if (node) {
        const tx = node.pos[0] + (centerOffset?.[0] || 0);
        const ty = node.pos[1] + (centerOffset?.[1] || 0);
        const tz = node.pos[2] + (centerOffset?.[2] || 0);

        const targetVec = new THREE.Vector3(tx, ty, tz);
        const targetCamPos = new THREE.Vector3(tx + 20, ty + 15, tz + 20);

        controls.target.lerp(targetVec, 0.1);
        camera.position.lerp(targetCamPos, 0.1);
        controls.update();

        if (camera.position.distanceTo(targetCamPos) < 0.5) {
          lastTargetId.current = topNodeId;
        }
      }
    } else if (!autoFocus && lastTargetId.current !== topNodeId) {
      lastTargetId.current = topNodeId;
    }
  });

  useEffect(() => {
    if (!topNodeId) lastTargetId.current = null;
  }, [topNodeId]);

  return null;
}

// 7. 카메라 초기 설정 (데이터 로드 시 1회)
function CameraSetup({ nodeCount }: { nodeCount: number }) {
  const { camera } = useThree();
  const lastCount = useRef(-1);
  useEffect(() => {
    if (nodeCount > 0 && nodeCount !== lastCount.current) {
      const pos = Math.max(20, Math.sqrt(nodeCount) * 10);
      camera.position.set(pos, pos * 0.6, pos);
      camera.lookAt(0, 0, 0);
      lastCount.current = nodeCount;
    }
  }, [nodeCount, camera]);
  return null;
}

export default function QuantumCanvas({ data, openNodes, topNodeId, showPopups, autoFocus, onNodeSelect, onNodeClose, onNodeFocus, centerOffset = [0, 0, 0] }: any) {
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);
  if (!data) return null;
  const nodeCount = data.nodes?.length || 0;
  const isMonolith = data.render_type === "monolith";
  const isSettlement = data.render_type === "settlement";

  return (
    <div className="w-full h-full relative bg-white">
      <Canvas camera={{ fov: 35 }}>
        <color attach="background" args={["#ffffff"]} />
        <CameraSetup nodeCount={nodeCount} />
        <CameraController topNodeId={topNodeId} data={data} centerOffset={centerOffset} autoFocus={autoFocus} />
        
        <Grid infiniteGrid fadeDistance={500} sectionColor="#e2e8f0" cellColor="#f1f5f9" position={[0, -0.1, 0]} />
        <ambientLight intensity={0.8} />
        <pointLight position={[50, 50, 50]} intensity={2} />
        <spotLight position={[-50, 50, 50]} angle={0.2} penumbra={1} intensity={2} />
        
        <group position={centerOffset}>
          <Suspense fallback={null}>
            {isMonolith ? (
              <RawDataMonolith data={data} onClick={onNodeSelect} />
            ) : (
              data.nodes?.map((node: any) => (
                isSettlement ? (
                  <SettlementBar key={node.id} node={node} isHovered={hoveredNodeId === node.id} isSelected={openNodes.some((n: any) => n.id === node.id)} isTop={topNodeId === node.id} onHover={setHoveredNodeId} onClick={onNodeSelect} centerOffset={centerOffset} />
                ) : (
                  <Node key={node.id} node={node} isHovered={hoveredNodeId === node.id} isSelected={openNodes.some((n: any) => n.id === node.id)} isTop={topNodeId === node.id} onHover={setHoveredNodeId} onClick={onNodeSelect} centerOffset={centerOffset} />
                )
              ))
            )}
            {!isMonolith && data.links?.map((link: any, idx: number) => {
              const source = data.nodes.find((n: any) => n.id === link.source);
              const target = data.nodes.find((n: any) => n.id === link.target);
              if (!source || !target) return null;
              const startPos = isSettlement ? [source.pos[0], 0, source.pos[2]] : source.pos;
              const endPos = isSettlement ? [target.pos[0], 0, target.pos[2]] : target.pos;
              
              const strength = link.strength || 5;
              const isStrong = strength >= 8;
              const isWeak = strength <= 4;
              const lineColor = isStrong ? "#2563eb" : (isWeak ? "#cbd5e1" : "#3b82f6");
              const lineWidth = isStrong ? 4 : (isWeak ? 0.5 : 1.5);
              const opacity = isStrong ? 1 : (isWeak ? 0.2 : 0.5);
              const isFocusRelated = topNodeId && (link.source === topNodeId || link.target === topNodeId);
              return <Line key={idx} points={[startPos, endPos]} color={isFocusRelated ? "#1e3a8a" : lineColor} lineWidth={isFocusRelated ? 5 : lineWidth} transparent opacity={isFocusRelated ? 1 : opacity} dashed={isWeak} />;
            })}
          </Suspense>
        </group>
        <OrbitControls makeDefault dampingFactor={0.15} enablePan={true} panSpeed={2.0} rotateSpeed={1.0} screenSpacePanning={true} mouseButtons={{ LEFT: THREE.MOUSE.PAN, MIDDLE: THREE.MOUSE.DOLLY, RIGHT: THREE.MOUSE.ROTATE }} keyEvents={true} />
      </Canvas>
    </div>
  );
}
