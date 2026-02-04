"use client";

import { useRef, Suspense, useState, useEffect } from "react";
import { Canvas, useFrame, useLoader } from "@react-three/fiber";
import { OrbitControls, Grid, Text, Float, Line, Html, Billboard, MeshWobbleMaterial } from "@react-three/drei";
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

// 2. 드래그 가능한 윈도우
function DraggableWindow({ node, onClose, zIndex, onFocus, isTop }: any) {
  const [pos, setPos] = useState({ x: 40 + Math.random() * 40, y: 120 + Math.random() * 60 });
  const [isDragging, setIsDragging] = useState(false);
  const offset = useRef({ x: 0, y: 0 });

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    onFocus(node.id);
    offset.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging) return;
      setPos({ x: e.clientX - offset.current.x, y: e.clientY - offset.current.y });
    };
    const handleMouseUp = () => setIsDragging(false);
    if (isDragging) {
      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);
    }
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [isDragging]);

  return (
    <div 
      onMouseDown={() => onFocus(node.id)}
      style={{ left: pos.x, top: pos.y, zIndex }}
      className={`absolute w-72 bg-slate-950/90 backdrop-blur-xl border-2 ${isTop ? 'border-yellow-400 shadow-[0_0_50px_rgba(234,179,8,0.4)]' : 'border-white/5 shadow-2xl'} rounded-2xl select-none overflow-hidden pointer-events-auto transition-all duration-300 transform ${isDragging ? 'scale-105 opacity-90' : 'scale-100 opacity-100'}`}
    >
      <div onMouseDown={handleMouseDown} className={`p-4 flex justify-between items-center cursor-move border-b border-white/5 ${isTop ? 'bg-yellow-500/20' : 'bg-blue-500/10'}`}>
        <div className="flex items-center gap-2 text-white">
          <div className={`w-2 h-2 rounded-full ${isTop ? 'bg-yellow-400' : 'bg-blue-400'} animate-pulse`} />
          <span className="text-[10px] font-black tracking-widest uppercase truncate w-40">{node.label}</span>
        </div>
        <button onClick={() => onClose(node.id)} className="text-slate-500 hover:text-white transition-colors">✕</button>
      </div>
      <div className="p-5 space-y-4">
        <div className="bg-black/60 p-4 rounded-xl border border-white/5 shadow-inner max-h-60 overflow-y-auto custom-scrollbar text-white">
          <p className="text-[11px] font-mono text-emerald-400 break-all leading-relaxed">
            {typeof node.value === 'string' ? node.value : JSON.stringify(node.value, null, 2)}
          </p>
        </div>
      </div>
    </div>
  );
}

// 3. 3D 노드 (일반)
function Node({ node, isHovered, isSelected, isTop, onHover, onClick }: any) {
  const color = isTop ? "#ffff00" : (isSelected ? "#00f2ff" : (isHovered ? "#ffffff" : node.color));
  const scale = isHovered ? 2.8 : (isTop ? 2.2 : 1.8);

  return (
    <group position={node.pos} onPointerOver={() => onHover(node.id)} onPointerOut={() => onHover(null)} onClick={() => onClick(node)}>
      <mesh visible={false}><sphereGeometry args={[2.0, 8, 8]} /></mesh>
      <Float speed={isTop ? 8 : 2} rotationIntensity={0.5} floatIntensity={0.5}>
        <mesh scale={scale}>
          <sphereGeometry args={[0.6, 32, 32]} />
          <meshStandardMaterial 
            color={color} 
            emissive={color} 
            emissiveIntensity={isTop ? 10 : (isSelected ? 5 : (isHovered ? 2 : 0.5))} 
            wireframe={!isTop && !isSelected && !isHovered} 
          />
        </mesh>
        {(isSelected || isTop) && <SelectionMarker color={isTop ? "#ffff00" : "#00f2ff"} isTop={isTop} />}
      </Float>
      <Billboard position={[0, 2.8, 0]}>
        <Text fontSize={isHovered || isTop ? 1.0 : 0.6} color={isTop ? "#ffff00" : (isSelected ? "#00f2ff" : (isHovered ? "white" : "rgba(255,255,255,0.2)"))} fontWeight="black" outlineWidth={0.05} outlineColor="black">
          {isTop ? `✓ ${node.label}` : node.label}
        </Text>
      </Billboard>
    </group>
  );
}

// 4. 정산 데이터를 위한 '바(Bar)' 컴포넌트
function SettlementBar({ node, isHovered, isSelected, isTop, onHover, onClick }: any) {
  const height = Math.max(2, node.pos[1]);
  const color = isTop ? "#ffff00" : (isSelected ? "#10b981" : "#38bdf8");
  
  return (
    <group position={[node.pos[0], 0, node.pos[2]]} onPointerOver={() => onHover(node.id)} onPointerOut={() => onHover(null)} onClick={() => onClick(node)}>
      <mesh position={[0, height / 2, 0]}>
        <boxGeometry args={[1.5, height, 1.5]} />
        <meshStandardMaterial 
          color={color} 
          emissive={color} 
          emissiveIntensity={isHovered || isTop ? 2 : 0.5} 
          transparent
          opacity={0.9}
        />
      </mesh>
      {(isSelected || isTop) && <SelectionMarker color={color} isTop={isTop} />}
      <Billboard position={[0, height + 1.5, 0]}>
        <Text fontSize={0.8} color={color} fontWeight="black" outlineWidth={0.05} outlineColor="black">
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
          <MeshWobbleMaterial color="#6366f1" factor={0.2} speed={1} emissive="#6366f1" emissiveIntensity={0.5} />
        </mesh>
      </Float>
      <Billboard position={[0, 6, 0]}>
        <Text fontSize={0.8} color="white" fontWeight="black">RAW_DATA_MONOLITH</Text>
        <Text position={[0, -0.8, 0]} fontSize={0.3} color="#818cf8">UNSTRUCTURED BLOB detected</Text>
      </Billboard>
    </group>
  );
}

export default function QuantumUniverse({ data, openNodes, topNodeId, showPopups, onNodeSelect, onNodeClose, onNodeFocus }: any) {
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);

  if (!data) return null;

  const nodeCount = data.nodes?.length || 0;
  const autoCameraPos = Math.max(20, Math.sqrt(nodeCount) * 10);
  const isMonolith = data.render_type === "monolith";
  const isSettlement = data.render_type === "settlement";

  return (
    <div className="w-full h-full relative bg-[#010409]">
      <Canvas camera={{ position: [autoCameraPos, autoCameraPos * 0.6, autoCameraPos], fov: 35 }}>
        <color attach="background" args={["#010409"]} />
        <Grid infiniteGrid fadeDistance={100} sectionColor="#1e293b" cellColor="#0f172a" position={[0, -0.1, 0]} />
        <ambientLight intensity={0.4} />
        <pointLight position={[50, 50, 50]} intensity={4} />
        <spotLight position={[-50, 50, 50]} angle={0.2} penumbra={1} intensity={3} />

        <Suspense fallback={null}>
          {isMonolith ? (
            <RawDataMonolith data={data} onClick={onNodeSelect} />
          ) : (
            data.nodes?.map((node: any) => (
              isSettlement ? (
                <SettlementBar 
                  key={node.id} 
                  node={node} 
                  isHovered={hoveredNodeId === node.id}
                  isSelected={openNodes.some((n: any) => n.id === node.id)}
                  isTop={topNodeId === node.id}
                  onHover={setHoveredNodeId}
                  onClick={onNodeSelect}
                />
              ) : (
                <Node 
                  key={node.id} 
                  node={node} 
                  isHovered={hoveredNodeId === node.id}
                  isSelected={openNodes.some((n: any) => n.id === node.id)}
                  isTop={topNodeId === node.id}
                  onHover={setHoveredNodeId}
                  onClick={onNodeSelect}
                />
              )
            ))
          )}

          {/* 연결선 */}
          {!isMonolith && data.links?.map((link: any, idx: number) => {
            const source = data.nodes.find((n: any) => n.id === link.source);
            const target = data.nodes.find((n: any) => n.id === link.target);
            if (!source || !target) return null;
            
            const startPos = isSettlement ? [source.pos[0], 0, source.pos[2]] : source.pos;
            const endPos = isSettlement ? [target.pos[0], 0, target.pos[2]] : target.pos;

            const isFocusRelated = topNodeId && (link.source === topNodeId || link.target === topNodeId);
            if (topNodeId && !openNodes.some((n: any) => link.source === n.id || link.target === n.id)) return null;
            
            return <Line key={idx} points={[startPos, endPos]} color={isFocusRelated ? "#ffff00" : "#38bdf8"} lineWidth={isFocusRelated ? 4 : 0.5} transparent opacity={isFocusRelated ? 1 : 0.15} />;
          })}
        </Suspense>
        <OrbitControls makeDefault dampingFactor={0.05} />
      </Canvas>

      {showPopups && (
        <div className="absolute inset-0 pointer-events-none overflow-hidden">
          {openNodes.map((node: any) => (
            <DraggableWindow 
              key={node.id} 
              node={node} 
              onClose={onNodeClose}
              onFocus={onNodeFocus}
              isTop={topNodeId === node.id}
              zIndex={topNodeId === node.id ? 100 : 10}
            />
          ))}
        </div>
      )}
    </div>
  );
}
