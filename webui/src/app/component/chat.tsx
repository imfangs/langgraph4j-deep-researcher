import { CopilotChat } from "@copilotkit/react-ui";
 
export function SimpleChat() {
  return (
    <CopilotChat
      labels={{
        title: "Your Researcher Assistant",
        initial: "Hi! 👋 What is the topic of your research?",
        placeholder: "Type your research topic here...",
      }}
      className="w-full"
    />
  );
}