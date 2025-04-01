import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import models
import logging

logger = logging.getLogger(__name__)

class EfficientDetector(nn.Module):
    def __init__(self, config):
        super(EfficientDetector, self).__init__()
        self.config = config

        # Load EfficientNet-B4 as the backbone
        self.backbone = models.efficientnet_b4(weights=models.EfficientNet_B4_Weights.DEFAULT)

        # Modify classifier to match binary classification (deepfake or real)
        in_features = self.backbone.classifier[1].in_features
        self.backbone.classifier = nn.Linear(in_features, 2)  # 2 output classes

        # Loss function
        self.loss_func = nn.CrossEntropyLoss()

    def features(self, data_dict):
        """Extract features from the EfficientNet backbone."""
        return self.backbone.features(data_dict['image'])

    def classifier(self, features):
        """Pass features through the classification head."""
        return self.backbone.classifier(features)

    def forward(self, data_dict, inference=False):
        """Forward pass: Extract features, classify, return probabilities."""
        x = data_dict['image']
        features = self.backbone.features(x)
        x = self.backbone.avgpool(features)
        x = torch.flatten(x, 1)
        x = self.backbone.classifier(x)

        prob = F.softmax(x, dim=1)[:, 1]  # Probability of being deepfake
        return {'cls': x, 'prob': prob, 'feat': features}

# Example usage
if __name__ == "__main__":
    # Create a dummy model
    config = {"backbone_name": "efficientnetb4", "loss_func": "cross_entropy"}
    model = EfficientDetector(config)

    # Create a dummy input image tensor (1, 3, 256, 256)
    dummy_input = {"image": torch.randn(1, 3, 256, 256)}

    # Forward pass
    output = model(dummy_input)
    print("Deepfake Probability:", output["prob"].item())
