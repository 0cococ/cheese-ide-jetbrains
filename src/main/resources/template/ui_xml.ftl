import core from 'cheese-core';
const xml = core.ui.xml;
let button= xml.getRunActivity().findViewById(xml.getID("t2"));
button.setOnClickListener(() => {
console.log("我是id是t2的按钮，我被点击了")
});